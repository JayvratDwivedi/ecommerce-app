package com.nextphase.backend.service;

import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.WebOrder;
import com.nextphase.backend.model.dao.WebOrderDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private WebOrderDAO webOrderDAO;

    public OrderService(WebOrderDAO webOrderDAO) {
        this.webOrderDAO = webOrderDAO;
    }

    public List<WebOrder> getOrders(LocalUser localUser) {
        return webOrderDAO.findByUser(localUser);
    }
}
