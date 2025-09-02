package com.example.notification;

import com.example.notification.kafka.UserEvent;
import com.example.notification.service.EmailService;
import com.example.notification.listener.UserEventListener;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3026",
        "spring.mail.username=",
        "spring.mail.password=",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "app.mail.from=test-from@yoursite.local"
})
class UserEventListenerIT {

    @Autowired
    private UserEventListener listener;

    private static GreenMail greenMail;

    @BeforeAll
    static void startMail() {
        greenMail = new GreenMail(new ServerSetup(3026, null, "smtp"));
        greenMail.start();
    }

    @AfterAll
    static void stopMail() {
        if (greenMail != null) greenMail.stop();
    }

    @BeforeEach
    void cleanMail() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void shouldSendEmailWhenUserCreated() throws Exception {
        UserEvent event = new UserEvent();
        event.setOperation(UserEvent.Operation.CREATE);
        event.setUserId(1L);
        event.setEmail("user1@example.com");
        event.setTimestamp(Instant.now());

        listener.onUserEvent(event);

        greenMail.waitForIncomingEmail(1);
        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("user1@example.com");
        assertThat(messages[0].getSubject()).isEqualTo("Аккаунт создан");
        assertThat(messages[0].getContent().toString()).contains("Здравствуйте! Ваш аккаунт на сайте был успешно создан");
    }

    @Test
    void shouldSendEmailWhenUserDeleted() throws Exception {
        UserEvent event = new UserEvent();
        event.setOperation(UserEvent.Operation.DELETE);
        event.setUserId(2L);
        event.setEmail("user2@example.com");
        event.setTimestamp(Instant.now());

        listener.onUserEvent(event);

        greenMail.waitForIncomingEmail(1);
        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("user2@example.com");
        assertThat(messages[0].getSubject()).isEqualTo("Аккаунт удален");
        assertThat(messages[0].getContent().toString()).contains("Здравствуйте! Ваш аккаунт был удален");
    }
}