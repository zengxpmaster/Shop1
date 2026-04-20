package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.dto.InventoryReduceDTO;
import com.mall.common.dto.OrderCreateDTO;
import com.mall.common.dto.RefundDTO;
import com.mall.common.entity.Order;
import com.mall.common.entity.OrderItem;
import com.mall.common.feign.InventoryFeignClient;
import com.mall.common.feign.PaymentFeignClient;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @Override
    @Transactional
    public String createOrder(OrderCreateDTO dto) {
        String orderNo = UUID.randomUUID().toString().replace("-", "");

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 创建订单明细并扣减库存
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(orderItem);

            // 扣减库存
            InventoryReduceDTO reduceDTO = new InventoryReduceDTO();
            reduceDTO.setProductId(item.getProductId());
            reduceDTO.setQuantity(item.getQuantity());
            reduceDTO.setOrderNo(orderNo);
            inventoryFeignClient.reduceStock(reduceDTO);
        }

        return orderNo;
    }

    @Override
    public void paySuccess(String orderNo) {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, 1)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now());
        orderMapper.update(null, wrapper);
    }

    @Override
    public List<Order> listOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        return orderMapper.selectOne(wrapper);
    }

    @Override
    public void applyRefund(String orderNo, String reason) {
        // 更新订单状态为退款中
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, 5)
                .set(Order::getUpdateTime, LocalDateTime.now());
        orderMapper.update(null, wrapper);

        // 调用支付服务发起退款
        Order order = getOrderByOrderNo(orderNo);
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setOrderNo(orderNo);
        refundDTO.setUserId(order.getUserId());
        refundDTO.setAmount(order.getTotalAmount());
        refundDTO.setReason(reason);
        paymentFeignClient.refund(refundDTO);
    }
}
