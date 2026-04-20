package com.mall.common.feign;

import com.mall.common.dto.ProductDTO;
import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务Feign接口
 */
@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/product/{id}")
    R<ProductDTO> getProductById(@PathVariable("id") Long id);
}
