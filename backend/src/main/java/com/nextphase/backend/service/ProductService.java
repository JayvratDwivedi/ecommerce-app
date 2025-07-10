package com.nextphase.backend.service;

import com.nextphase.backend.model.Product;
import com.nextphase.backend.model.dao.ProductDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }
    public List<Product> getProducts(){
        return productDao.findAll();
    }
}
