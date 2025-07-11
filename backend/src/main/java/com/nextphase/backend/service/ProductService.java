package com.nextphase.backend.service;

import com.nextphase.backend.model.Product;
import com.nextphase.backend.model.dao.ProductDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private ProductDAO productDao;

    public ProductService(ProductDAO productDao) {
        this.productDao = productDao;
    }
    public List<Product> getProducts(){
        return productDao.findAll();
    }
}
