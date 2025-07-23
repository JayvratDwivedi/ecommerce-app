package com.nextphase.backend.api.controller.user;

import com.nextphase.backend.model.Address;
import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.dao.AddressDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    private AddressDAO addressDAO;

    public UserController(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@PathVariable Long userId){
        return ResponseEntity.ok(addressDAO.findByUser_Id(userId));
    }

//    @GetMapping("/{userId}/address")
//    public ResponseEntity<List<Address>> getAddress(
//            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId) {
//        if (!userHasPermission(user, userId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//        return ResponseEntity.ok(addressDAO.findByUser_Id(userId));
//    }
//
//    private boolean userHasPermission(LocalUser user, Long id) {
//        return Objects.equals(user.getId(), id);
//    }



}
