package com.mall.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 */
@Data
public class RefundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String reason;
}
