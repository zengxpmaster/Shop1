package com.mall.product.controller;

import com.mall.common.dto.ProductDTO;
import com.mall.common.entity.Product;
import com.mall.common.result.R;
import com.mall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public R<ProductDTO> getProductById(@PathVariable Long id) {
        return R.ok(productService.getProductById(id));
    }

    @GetMapping("/list")
    public R<List<Product>> listProducts() {
        return R.ok(productService.listProducts());
    }

    @PostMapping
    public R<Void> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return R.ok();
    }

    @PutMapping
    public R<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return R.ok();
    }
}
