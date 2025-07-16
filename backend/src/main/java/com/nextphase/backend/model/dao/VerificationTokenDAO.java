package com.nextphase.backend.model.dao;

import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByLocalUser(LocalUser user);
    List<VerificationToken> findByLocalUser_IdOrderByIdDesc(Long id);
}
