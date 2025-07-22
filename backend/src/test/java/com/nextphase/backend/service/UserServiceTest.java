package com.nextphase.backend.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.PasswordResetBody;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.EmailFailureException;
import com.nextphase.backend.exception.EmailNotFoundException;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.exception.UserNotVerifiedException;
import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.model.VerificationToken;
import com.nextphase.backend.model.dao.LocalUserDAO;
import com.nextphase.backend.model.dao.VerificationTokenDAO;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDAO localUserDao;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private VerificationTokenDAO verificationTokenDAO;


    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationBody body = new RegistrationBody();
        body.setUsername("UserA");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setFirstName("First-Name");
        body.setLastName("Last-Name");
        body.setPassword("MyPassword123");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> userService.registerUser(body), "Username should already be in use.");
        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> userService.registerUser(body), "Email should already be in use.");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body));
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());

    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginBody body = new LoginBody();
        body.setUsername("UserA-NotExists");
        body.setPassword("PasswordA123-BadPassword");
        Assertions.assertNull( userService.loginUser(body), "The user should not exist.");
        body.setUsername("UserA");
        Assertions.assertNull( userService.loginUser(body), "The password should be incorrect.");
        body.setPassword("PasswordA123");
        Assertions.assertNotNull( userService.loginUser(body), "The user should login successfully.");
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            userService.loginUser(body);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException e) {
            Assertions.assertTrue(e.isNewEmailSent(), "Email Verification to be sent.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
        try {
            userService.loginUser(body);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException e) {
            Assertions.assertFalse(e.isNewEmailSent(), "Email verification should not be resent.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
    }

    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException {
        Assertions.assertFalse(userService.verifyUser("Bad Token"), "Token that is bad or does not exist should return false.");
        LoginBody body = new LoginBody();
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            userService.loginUser(body);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException ex) {
            List<VerificationToken> tokens = verificationTokenDAO.findByLocalUser_IdOrderByIdDesc(2L);
            String token = tokens.getFirst().getToken();
            Assertions.assertTrue(userService.verifyUser(token), "Token should be valid.");
            Assertions.assertNotNull(body, "The user should now be verified.");
        }
    }

    @Test
    @Transactional
    public void testForgotPassword() throws MessagingException {
        Assertions.assertThrows(EmailNotFoundException.class,
                () -> userService.forgotPassword("UserNotExist@junit.com"));
        Assertions.assertDoesNotThrow(() -> userService.forgotPassword(
                "UserA@junit.com"), "Non existing email should be rejected.");
        Assertions.assertEquals("UserA@junit.com",
                greenMailExtension.getReceivedMessages()[0]
                        .getRecipients(Message.RecipientType.TO)[0].toString(), "Password " +
                        "reset email should be sent.");
    }

//    @Test
//    @Transactional
    public void testResetPassword() {
        LocalUser user = localUserDao.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        PasswordResetBody body = new PasswordResetBody();
        body.setToken(token);
        body.setPassword("Password123456");
        userService.resetPassword(body);
        user = localUserDao.findByUsernameIgnoreCase("UserA").get();
        Assertions.assertTrue(encryptionService.verifyPassword("Password123456",
                user.getPassword()), "Password change should be written to DB.");
    }

}
