package com.mall.order.controller;

import com.mall.common.dto.OrderCreateDTO;
import com.mall.common.entity.Order;
import com.mall.common.result.R;
import com.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public R<String> createOrder(@RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createOrder(dto);
        return R.ok(orderNo);
    }

    @PostMapping("/paySuccess")
    public R<Void> paySuccess(@RequestParam String orderNo) {
        orderService.paySuccess(orderNo);
        return R.ok();
    }

    @GetMapping("/list")
    public R<List<Order>> listOrders(@RequestParam Long userId) {
        return R.ok(orderService.listOrdersByUserId(userId));
    }

    @GetMapping("/{orderNo}")
    public R<Order> getOrder(@PathVariable String orderNo) {
        return R.ok(orderService.getOrderByOrderNo(orderNo));
    }

    @PostMapping("/refund")
    public R<Void> applyRefund(@RequestParam String orderNo, @RequestParam String reason) {
        orderService.applyRefund(orderNo, reason);
        return R.ok();
    }
}
