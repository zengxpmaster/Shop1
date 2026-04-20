# 软件详细设计说明书

---

#### 1. 引言
本文档详细描述了分销商城系统的具体实现细节，包括开发环境、核心类定义、详细接口逻辑及数据库操作规范，旨在指导开发人员进行代码编写。

#### 2. 开发环境与依赖
*   **JDK版本**：1.8 或以上
*   **构建工具**：Maven 3.6+
*   **核心依赖 (pom.xml)**：
    *   `spring-boot-starter-web` (Web服务)
    *   `spring-boot-starter-data-redis` (Redis会话管理)
    *   `spring-boot-starter-amqp` (RabbitMQ消息队列)
    *   `mybatis-spring-boot-starter` (ORM框架)
    *   `mysql-connector-java` (数据库驱动)
    *   `lombok` (简化实体类代码)

#### 3. 领域模型设计 (Entity)
根据数据库表结构，定义Java实体类。

**3.1 用户实体 (User)**
*   **类名**：`UserInfo`
*   **属性**：
    *   `private Long id;`
    *   `private String phone;`
    *   `private String password;`
    *   `private BigDecimal balance;` (余额)
    *   `private Long parentId;` (上级ID，自关联)
    *   `private Date createTime;`

**3.2 订单实体 (Order)**
*   **类名**：`OrderInfo`
*   **属性**：
    *   `private Long id;`
    *   `private String orderNo;`
    *   `private Long userId;`
    *   `private Long productId;`
    *   `private String productName;` (快照)
    *   `private BigDecimal totalPrice;`
    *   `private Integer status;` (10:待支付, 20:待收货, 30:已完成)
    *   `private Date payTime;`

**3.3 返利记录实体 (RebateRecord)**
*   **类名**：`RebateRecord`
*   **属性**：
    *   `private Long id;`
    *   `private Long orderId;`
    *   `private Long userId;`
    *   `private BigDecimal amount;`
    *   `private Integer type;` (1:一级, 2:二级)
    *   `private Date createTime;`

#### 4. 核心功能详细设计

**4.1 用户认证与Redis会话管理**
*   **设计目标**：实现无状态Token登录，利用Redis存储Token映射。
*   **流程逻辑**：
    1.  **LoginService**：校验手机号密码。
    2.  **Token生成**：使用 `UUID.randomUUID().toString()` 生成Token。
    3.  **Redis存储**：`redisTemplate.opsForValue().set("token:" + token, userId.toString(), 1, TimeUnit.HOURS);`
    4.  **拦截器 (Interceptor)**：在 `preHandle` 中校验Header中的Token是否存在且未过期。

**4.2 订单状态机与RabbitMQ异步解耦**
*   **设计目标**：用户确认收货后，通过MQ异步触发佣金计算，避免阻塞主线程。
*   **流程逻辑**：
    1.  **Controller**：接收确认收货请求。
    2.  **OrderService**：
        *   校验订单状态是否为“待收货”。
        *   更新订单状态为“已完成”。
        *   **发送消息**：`rabbitTemplate.convertAndSend("commission_exchange", "commission.routing.key", orderId);`
    3.  **MQ Consumer (RebateListener)**：
        *   监听队列 `commission.queue`。
        *   接收 `orderId`。
        *   **开启事务**：`@Transactional`。
        *   **计算逻辑**：查询订单 -> 查询商品佣金 -> 查询上级ID。
        *   **更新余额**：`userMapper.updateBalance(parentId, commissionAmount)`。
        *   **写入记录**：插入一条 `rebate_record`。

**4.3 分销关系绑定**
*   **设计目标**：新用户通过邀请链接注册时，记录其上级。
*   **逻辑**：
    *   注册接口增加参数 `inviteId`。
    *   逻辑判断：若该用户是首次注册（或首次访问带有inviteId的链接），则将 `parent_id` 写入数据库。

#### 5. 接口详细实现说明

**5.1 确认收货接口 (OrderController)**
*   **路径**：`POST /api/order/confirm`
*   **参数**：`orderId`
*   **代码逻辑伪代码**：
```java
    public Result confirmOrder(Long orderId, @RequestHeader("Token") String token) {
        // 1. 校验Token与用户身份
        Long userId = redisService.getUserIdByToken(token);
        
        // 2. 校验订单归属
        OrderInfo order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        
        // 3. 校验状态（必须是待收货）
        if (order.getStatus() != 20) {
            return Result.error("订单状态不正确");
        }
        
        // 4. 更新状态为已完成
        order.setStatus(30);
        order.setPayTime(new Date()); // 模拟支付时间
        orderMapper.updateById(order);
        
        // 5. 发送MQ消息（异步）
        rabbitTemplate.convertAndSend(RabbitConfig.COMMISSION_EXCHANGE, 
                                     RabbitConfig.COMMISSION_ROUTING_KEY, 
                                     order.getId());
        
        return Result.success("收货成功，返利处理中");
    }
    ```

**5.2 返利消费者 (RebateListener)**
*   **代码逻辑伪代码**：
```java
    @RabbitListener(queues = RabbitConfig.COMMISSION_QUEUE)
    @Transactional(rollbackFor = Exception.class) // 关键：保证数据一致性
    public void handleCommission(Long orderId, Channel channel, Message message) {
        try {
            // 1. 查询订单详情
            OrderInfo order = orderMapper.selectById(orderId);
            if (order == null) return;
            
            // 2. 查询商品佣金
            ProductInfo product = productMapper.selectById(order.getProductId());
            BigDecimal commission = product.getCommissionAmount();
            
            // 3. 查询上级用户ID
            UserInfo user = userInfoMapper.selectById(order.getUserId());
            Long parentId = user.getParentId();
            
            if (parentId != null && parentId > 0) {
                // 4. 更新上级余额
                userInfoMapper.updateBalanceAdd(parentId, commission);
                
                // 5. 插入返利记录
                RebateRecord record = new RebateRecord();
                record.setOrderId(orderId);
                record.setUserId(parentId);
                record.setAmount(commission);
                record.setType(1); // 一级分销
                rebateRecordMapper.insert(record);
            }
            
            // 6. 手动ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            // 处理异常，记录日志，拒绝消息或进入死信队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
    ```

#### 6. 数据库操作规范
1.  **事务边界**：所有涉及资金变动（余额更新）的操作，必须在Service层使用 `@Transactional` 注解，确保原子性。
2.  **乐观锁**：在高并发扣减库存场景下（虽然本项目为模拟，但建议预留），建议在 `product` 表增加 `version` 字段。
3.  **SQL编写**：严禁使用 `SELECT *`，必须明确指定字段名。

#### 7. 异常处理设计
*   **全局异常处理器**：`@ControllerAdvice` 统一捕获 `Exception` 和自定义业务异常 `BusinessException`。
*   **MQ异常**：消费者必须捕获异常并进行 `Nack` 或记录日志，防止消息丢失。
*   **常见错误码**：
    *   `401`: Token失效或未登录。
    *   `500`: 服务器内部错误。
    *   `1001`: 业务逻辑错误（如余额不足、状态错误）。

#### 8. 部署配置建议
*   **Redis配置**：建议设置最大内存策略 `maxmemory-policy allkeys-lru`。
*   **RabbitMQ配置**：
    *   队列声明为 **Durable (持久化)**。
    *   消息发送设置为 **Persistent**。
    *   消费者开启 **Manual Ack** (手动确认)。

---

### 附录：开发任务清单 (Checklist)
1.  [ ] 初始化Spring Boot项目，配置MyBatis与MySQL连接。
2.  [ ] 集成Redis，实现Token登录拦截器。
3.  [ ] 实现商品列表与用户注册登录接口。
4.  [ ] 集成RabbitMQ，配置交换机与队列。
5.  [ ] 编写订单确认收货逻辑（发送MQ）。
6.  [ ] 编写MQ消费者逻辑（更新余额+记录）。
7.  [ ] 前后端联调，测试完整分销流程。
