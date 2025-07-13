package com.nextphase.backend.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.UserAlreadyExistException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LocalUserServiceTest {

    @Autowired
    private LocalUserService localUserService;

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

}
