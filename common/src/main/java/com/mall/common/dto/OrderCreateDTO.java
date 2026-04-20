package com.mall.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
