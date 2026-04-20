package com.mall.inventory.controller;

import com.mall.common.dto.InventoryReduceDTO;
import com.mall.common.result.R;
import com.mall.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/reduce")
    public R<Void> reduceStock(@RequestBody InventoryReduceDTO dto) {
        inventoryService.reduceStock(dto);
        return R.ok();
    }

    @PostMapping("/restore")
    public R<Void> restoreStock(@RequestBody InventoryReduceDTO dto) {
        inventoryService.restoreStock(dto);
        return R.ok();
    }

    @GetMapping("/{productId}")
    public R<Integer> getStock(@PathVariable Long productId) {
        return R.ok(inventoryService.getStock(productId));
    }
}
