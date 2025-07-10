package com.nextphase.backend.service;

import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.WebOrder;
import com.nextphase.backend.model.dao.WebOrderDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private WebOrderDao webOrderDao;

    public OrderService(WebOrderDao webOrderDao) {
        this.webOrderDao = webOrderDao;
    }

    public List<WebOrder> getOrders(LocalUser localUser){
        return webOrderDao.findByLocalUser(localUser);
    }
}
