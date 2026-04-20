package com.mall.order.service;

import com.mall.common.dto.OrderCreateDTO;
import com.mall.common.entity.Order;

import java.util.List;

public interface OrderService {

    String createOrder(OrderCreateDTO dto);

    void paySuccess(String orderNo);

    List<Order> listOrdersByUserId(Long userId);

    Order getOrderByOrderNo(String orderNo);

    void applyRefund(String orderNo, String reason);
}
