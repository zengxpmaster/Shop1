package com.mall.payment.service;

import com.mall.common.dto.RefundDTO;

import java.math.BigDecimal;

public interface PaymentService {

    void pay(String orderNo, Long userId, BigDecimal amount);

    void refund(RefundDTO dto);

    BigDecimal getBalance(Long userId);
}
