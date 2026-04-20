package com.mall.payment.controller;

import com.mall.common.dto.RefundDTO;
import com.mall.common.result.R;
import com.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay")
    public R<Void> pay(@RequestParam String orderNo, @RequestParam Long userId, @RequestParam BigDecimal amount) {
        paymentService.pay(orderNo, userId, amount);
        return R.ok();
    }

    @PostMapping("/refund")
    public R<Void> refund(@RequestBody RefundDTO dto) {
        paymentService.refund(dto);
        return R.ok();
    }

    @GetMapping("/balance/{userId}")
    public R<BigDecimal> getBalance(@PathVariable Long userId) {
        return R.ok(paymentService.getBalance(userId));
    }
}
