package com.nextphase.backend.model.dao;

import com.nextphase.backend.model.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductDao extends ListCrudRepository<Product, Long> { }
