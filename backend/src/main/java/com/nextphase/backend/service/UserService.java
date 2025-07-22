package com.nextphase.backend.service;

import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.PasswordResetBody;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.EmailFailureException;
import com.nextphase.backend.exception.EmailNotFoundException;
import com.nextphase.backend.exception.UserNotVerifiedException;
import com.nextphase.backend.model.VerificationToken;
import com.nextphase.backend.model.dao.LocalUserDAO;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.dao.VerificationTokenDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private LocalUserDAO localUserDao;
    private EncryptionService encryptionService;
    private JWTService jwtService;
    private EmailService emailService;
    private VerificationTokenDAO verificationTokenDAO;

    public UserService(LocalUserDAO localUserDao, EncryptionService encryptionService,
                       JWTService jwtService, EmailService emailService, VerificationTokenDAO verificationTokenDAO) {
        this.localUserDao = localUserDao;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenDAO = verificationTokenDAO;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistException, EmailFailureException {
        if(localUserDao.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDao.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistException();
        }
        LocalUser localUser = new LocalUser();
        localUser.setUsername(registrationBody.getUsername());
        localUser.setEmail(registrationBody.getEmail());
        localUser.setFirstname(registrationBody.getFirstName());
        localUser.setLastname(registrationBody.getLastName());
        localUser.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken = createVerificationToken(localUser);
        emailService.sendVerificationEmail(verificationToken);
        return localUserDao.save(localUser);
    }

    private VerificationToken createVerificationToken(LocalUser localUser) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(localUser));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setLocalUser(localUser);
        localUser.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> localUser = localUserDao.findByUsernameIgnoreCase(loginBody.getUsername());
        if(localUser.isPresent()) {
            LocalUser user = localUser.get();
            if(encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if(user.isEmailVerified()) {
                    return jwtService.generateJWT(user);
                }
                else {
                    List<VerificationToken> verificationTokens= user.getVerificationTokens();
                    boolean resend = verificationTokens.isEmpty() ||
                            verificationTokens.getFirst().getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - 60 * 60 *1000));
                    if(resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }

                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);
        if(opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getLocalUser();
            if(!user.isEmailVerified()) {
                user.setEmailVerified(true);
                localUserDao.save(user);
                verificationTokenDAO.deleteByLocalUser(user);
                return true;
            }
        }
        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> opUser = localUserDao.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            String token = jwtService.generatePasswordResetJWT(user);
            emailService.sendPasswordResetEmail(user, token);
        } else {
            throw new EmailNotFoundException();
        }
    }

    public void resetPassword(PasswordResetBody body) {
        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional<LocalUser> opUser = localUserDao.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            user.setPassword(encryptionService.encryptPassword(body.getPassword()));
            localUserDao.save(user);
        }
    }

}
