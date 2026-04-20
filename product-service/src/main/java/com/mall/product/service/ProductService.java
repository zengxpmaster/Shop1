package com.mall.product.service;

import com.mall.common.dto.ProductDTO;
import com.mall.common.entity.Product;

import java.util.List;

public interface ProductService {

    ProductDTO getProductById(Long id);

    List<Product> listProducts();

    void addProduct(Product product);

    void updateProduct(Product product);

    void deleteProduct(Long id);
}
