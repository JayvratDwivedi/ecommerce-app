package com.nextphase.backend.model.dao;

import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface WebOrderDAO extends ListCrudRepository<WebOrder, Long> {
    List<WebOrder> findByUser(LocalUser localUser);
}
