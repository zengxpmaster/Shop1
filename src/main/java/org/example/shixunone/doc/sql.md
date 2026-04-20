很高兴为你继续完善系统设计。基于上一阶段的《系统模块设计文档》，我为你整理了详细的《数据库设计文档》。

这份文档明确了支撑分销商城系统所需的5张核心表结构，并使用Mermaid语法绘制了ER图，确保数据的一致性和完整性。

---

### 数据库设计文档

#### 1. 概述
本系统采用MySQL作为核心存储，设计遵循第三范式（3NF），主要包含用户、商品、订单、返利记录及商品库存流水表。设计重点在于处理**分销关系（自关联）**和**资金流水的安全性（事务控制）**。

#### 2. 命名规范
*   **表名**：小写，单词间用下划线 `_` 分隔（如 `user_info`）。
*   **字段名**：小写，单词间用下划线 `_` 分隔。
*   **主键**：统一使用 `id`，类型为 `BIGINT`，自增或UUID。
*   **时间字段**：统一包含 `create_time` 和 `update_time`。

#### 3. 表结构详细设计

**3.1 用户表 (user_info)**
*   **描述**：存储用户基本信息及资产余额。
*   **主键**：`id`
*   **唯一约束**：`phone`

| 字段名 | 数据类型 | 允许空 | 默认值 | 主键 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 否 | - | 是 | 用户ID (主键) |
| phone | VARCHAR(11) | 否 | - | 否 | 手机号 (唯一登录凭证) |
| password | VARCHAR(64) | 否 | - | 否 | 密码 (SHA256加密) |
| balance | DECIMAL(10,2) | 否 | 0.00 | 否 | 账户余额 |
| parent_id | BIGINT | 是 | NULL | 否 | 上级用户ID (自关联外键) |
| create_time | DATETIME | 否 | - | 否 | 创建时间 |
| update_time | DATETIME | 否 | - | 否 | 更新时间 |

**3.2 商品表 (product_info)**
*   **描述**：存储商品基础信息及分销佣金比例。
*   **主键**：`id`

| 字段名 | 数据类型 | 允许空 | 默认值 | 主键 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 否 | - | 是 | 商品ID |
| name | VARCHAR(100) | 否 | - | 否 | 商品名称 |
| price | DECIMAL(10,2) | 否 | - | 否 | 销售价格 |
| stock | INT | 否 | 0 | 否 | 库存数量 |
| commission_rate | DECIMAL(5,4) | 否 | 0.1000 | 否 | 佣金比例 (如0.1代表10%) |
| status | TINYINT | 否 | 1 | 否 | 状态 (0-下架, 1-上架) |
| create_time | DATETIME | 否 | - | 否 | 创建时间 |

**3.3 订单表 (order_info)**
*   **描述**：存储交易订单信息。
*   **主键**：`id`
*   **外键**：`user_id` -> `user_info.id`, `product_id` -> `product_info.id`

| 字段名 | 数据类型 | 允许空 | 默认值 | 主键 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 否 | - | 是 | 订单ID |
| order_no | VARCHAR(32) | 否 | - | 否 | 订单编号 (全局唯一) |
| user_id | BIGINT | 否 | - | 否 | 下单用户ID |
| product_id | BIGINT | 否 | - | 否 | 商品ID |
| product_name | VARCHAR(100) | 否 | - | 否 | 商品快照名称 |
| quantity | INT | 否 | 1 | 否 | 购买数量 |
| total_price | DECIMAL(10,2) | 否 | - | 否 | 总价 |
| status | TINYINT | 否 | 10 | 否 | 状态 (10-待支付, 20-待收货, 30-已完成) |
| create_time | DATETIME | 否 | - | 否 | 创建时间 |
| pay_time | DATETIME | 是 | NULL | 否 | 支付时间 |

**3.4 返利记录表 (rebate_record)**
*   **描述**：记录每一次佣金的产生和归属。
*   **主键**：`id`
*   **外键**：`order_id` -> `order_info.id`, `user_id` -> `user_info.id`

| 字段名 | 数据类型 | 允许空 | 默认值 | 主键 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 否 | - | 是 | 记录ID |
| order_id | BIGINT | 否 | - | 否 | 关联订单ID |
| user_id | BIGINT | 否 | - | 否 | 获得佣金的用户ID |
| amount | DECIMAL(10,2) | 否 | - | 否 | 佣金金额 |
| type | TINYINT | 否 | 1 | 否 | 返利类型 (1-一级分销, 2-二级分销) |
| create_time | DATETIME | 否 | - | 否 | 记录时间 |

**3.5 商品库存流水表 (stock_transaction)**
*   **描述**：记录库存的每一次变动，用于对账和回滚。
*   **主键**：`id`

| 字段名 | 数据类型 | 允许空 | 默认值 | 主键 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 否 | - | 是 | 流水ID |
| product_id | BIGINT | 否 | - | 否 | 商品ID |
| order_id | BIGINT | 否 | - | 否 | 关联订单ID |
| change_type | TINYINT | 否 | - | 否 | 变动类型 (1-扣减, 2-回滚) |
| change_count | INT | 否 | - | 否 | 变动数量 |
| before_count | INT | 否 | - | 否 | 变动前库存 |
| after_count | INT | 否 | - | 否 | 变动后库存 |
| create_time | DATETIME | 否 | - | 否 | 操作时间 |

---

### 4. 数据库 ER 图


### 5. 设计要点说明
1.  **自关联设计**：`user_info` 表中的 `parent_id` 字段指向自身的 `id`，用于构建无限级分销关系树，但在本需求中通常只追溯一级或两级上级。
2.  **数据一致性**：`order_info` 表中冗余了 `product_name` 字段，这是为了防止商品名称修改后，历史订单显示错误（快照思想）。
3.  **流水化设计**：`stock_transaction` 表不采用直接更新库存的方式，而是记录流水，便于排查超卖问题和进行库存回滚操作。