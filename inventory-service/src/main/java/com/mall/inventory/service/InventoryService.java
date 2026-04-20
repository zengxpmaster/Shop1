package com.mall.inventory.service;

import com.mall.common.dto.InventoryReduceDTO;

public interface InventoryService {

    void reduceStock(InventoryReduceDTO dto);

    void restoreStock(InventoryReduceDTO dto);

    Integer getStock(Long productId);
}
