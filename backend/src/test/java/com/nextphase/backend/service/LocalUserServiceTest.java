package com.nextphase.backend.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.EmailFailureException;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.exception.UserNotVerifiedException;
import com.nextphase.backend.model.VerificationToken;
import com.nextphase.backend.model.dao.VerificationTokenDAO;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class LocalUserServiceTest {

    @Autowired
    private LocalUserService localUserService;

    @Autowired
    private VerificationTokenDAO verificationTokenDAO;


    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationBody registrationBody = new RegistrationBody();
        registrationBody.setUsername("UserA");
        registrationBody.setEmail("UserServiceTest$testRegisterUser@junit.com");
        registrationBody.setFirstName("First-Name");
        registrationBody.setLastName("Last-Name");
        registrationBody.setPassword("MyPassword123");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> localUserService.registerUser(registrationBody), "Username should already be in use.");
        registrationBody.setUsername("UserServiceTest$testRegisterUser");
        registrationBody.setEmail("UserA@junit.com");
        Assertions.assertThrows(UserAlreadyExistException.class,
                () -> localUserService.registerUser(registrationBody), "Email should already be in use.");
        registrationBody.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> localUserService.registerUser(registrationBody));
        Assertions.assertEquals(registrationBody.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());

    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginBody loginBody = new LoginBody();
        loginBody.setUsername("UserA-NotExists");
        loginBody.setPassword("PasswordA123-BadPassword");
        Assertions.assertNull( localUserService.loginUser(loginBody), "The user should not exist.");
        loginBody.setUsername("UserA");
        Assertions.assertNull( localUserService.loginUser(loginBody), "The password should be incorrect.");
        loginBody.setPassword("PasswordA123");
        Assertions.assertNotNull( localUserService.loginUser(loginBody), "The user should login successfully.");
        loginBody.setUsername("UserB");
        loginBody.setPassword("PasswordB123");
        try {
            localUserService.loginUser(loginBody);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException e) {
            Assertions.assertTrue(e.isNewEmailSent(), "Email Verification to be sent.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
        try {
            localUserService.loginUser(loginBody);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException e) {
            Assertions.assertFalse(e.isNewEmailSent(), "Email verification should not be resent.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
    }

    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException {
        Assertions.assertFalse(localUserService.verifyUser("Bad Token"), "Token that is bad or does not exist should return false.");
        LoginBody body = new LoginBody();
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            localUserService.loginUser(body);
            Assertions.fail("User should not have email verified.");
        } catch (UserNotVerifiedException ex) {
            List<VerificationToken> tokens = verificationTokenDAO.findByLocalUser_IdOrderByIdDesc(2L);
            String token = tokens.getFirst().getToken();
            Assertions.assertTrue(localUserService.verifyUser(token), "Token should be valid.");
            Assertions.assertNotNull(body, "The user should now be verified.");
        }
    }

}
