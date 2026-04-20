package com.mall.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存扣减请求DTO
 */
@Data
public class InventoryReduceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;
    private String orderNo;
}
