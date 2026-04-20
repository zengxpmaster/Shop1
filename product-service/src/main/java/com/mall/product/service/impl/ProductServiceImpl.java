package com.mall.product.service.impl;

import com.mall.common.dto.ProductDTO;
import com.mall.common.entity.Product;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }

    @Override
    public List<Product> listProducts() {
        return productMapper.selectList(null);
    }

    @Override
    public void addProduct(Product product) {
        product.setStatus(1);
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
