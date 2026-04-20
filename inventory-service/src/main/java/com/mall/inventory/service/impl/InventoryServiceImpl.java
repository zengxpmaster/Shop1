package com.mall.inventory.service.impl;

import com.mall.common.dto.InventoryReduceDTO;
import com.mall.common.entity.Inventory;
import com.mall.common.entity.InventoryLog;
import com.mall.inventory.mapper.InventoryLogMapper;
import com.mall.inventory.mapper.InventoryMapper;
import com.mall.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    @Transactional
    public void reduceStock(InventoryReduceDTO dto) {
        Inventory inventory = inventoryMapper.selectById(dto.getProductId());
        if (inventory == null) {
            throw new RuntimeException("商品库存记录不存在");
        }
        if (inventory.getStock() < dto.getQuantity()) {
            throw new RuntimeException("库存不足");
        }
        inventory.setStock(inventory.getStock() - dto.getQuantity());
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        // 记录库存变更流水
        InventoryLog log = new InventoryLog();
        log.setProductId(dto.getProductId());
        log.setChangeQuantity(-dto.getQuantity());
        log.setType("REDUCE");
        log.setOrderNo(dto.getOrderNo());
        log.setCreateTime(LocalDateTime.now());
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void restoreStock(InventoryReduceDTO dto) {
        Inventory inventory = inventoryMapper.selectById(dto.getProductId());
        if (inventory == null) {
            throw new RuntimeException("商品库存记录不存在");
        }
        inventory.setStock(inventory.getStock() + dto.getQuantity());
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        // 记录库存变更流水
        InventoryLog log = new InventoryLog();
        log.setProductId(dto.getProductId());
        log.setChangeQuantity(dto.getQuantity());
        log.setType("RESTORE");
        log.setOrderNo(dto.getOrderNo());
        log.setCreateTime(LocalDateTime.now());
        inventoryLogMapper.insert(log);
    }

    @Override
    public Integer getStock(Long productId) {
        Inventory inventory = inventoryMapper.selectById(productId);
        return inventory != null ? inventory.getStock() : 0;
    }
}
