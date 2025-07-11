package com.nextphase.backend.service;

import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.model.VerificationToken;
import com.nextphase.backend.model.dao.LocalUserDao;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.model.LocalUser;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class LocalUserService {
    private LocalUserDao localUserDao;
    private EncryptionService encryptionService;
    private JWTService jwtService;

    public LocalUserService(LocalUserDao localUserDao, EncryptionService encryptionService, JWTService jwtService) {
        this.localUserDao = localUserDao;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistException {
        LocalUser localUser = new LocalUser();
        if(localUserDao.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDao.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()){
            throw new UserAlreadyExistException("User already exist...");
        }
        localUser.setUsername(registrationBody.getUsername());
        localUser.setEmail(registrationBody.getEmail());
        localUser.setFirstname(registrationBody.getFirstName());
        localUser.setLastname(registrationBody.getLastName());
        localUser.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken = createVerificationToken(localUser);
        return localUserDao.save(localUser);
    }

    private VerificationToken createVerificationToken(LocalUser localUser){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(localUser));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setLocalUser(localUser);
        localUser.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    public String loginUser(LoginBody loginBody){
        Optional<LocalUser> localUser = localUserDao.findByUsernameIgnoreCase(loginBody.getUsername());
        if(localUser.isPresent()){
            LocalUser user = localUser.get();
            if(encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())){
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }
}
