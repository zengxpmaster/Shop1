package com.mall.common.feign;

import com.mall.common.dto.InventoryReduceDTO;
import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务Feign接口
 */
@FeignClient(name = "inventory-service")
public interface InventoryFeignClient {

    @PostMapping("/inventory/reduce")
    R<Void> reduceStock(@RequestBody InventoryReduceDTO dto);

    @PostMapping("/inventory/restore")
    R<Void> restoreStock(@RequestBody InventoryReduceDTO dto);
}
