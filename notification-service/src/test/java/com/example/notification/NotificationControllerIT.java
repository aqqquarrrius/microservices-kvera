package com.example.notification;


import com.example.notification.dto.SendEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "string.mail.username=",
        "string.mail.password=",
        "string.mail.properties.mail.smtp.auth=false",
        "string.mail.properties.mail.smtp.starttls.enable=false",
        "app.mail.from=test-from@yoursite.local"
})
public class NotificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static GreenMail greenMail;

    @BeforeAll
    static void startGreenMail() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();
    }

    @AfterAll
    static void stopGreenMail() {
        if (greenMail != null) greenMail.stop();
    }

    @Test
    void shouldSendEmailViaRestApi() throws Exception {
        SendEmailRequest req = new SendEmailRequest(
                "user@example.com",
                "Тест тема",
                "Здравствуйте! Это тестовое письмо"
        );

        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted());

        greenMail.waitForIncomingEmail(1);
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);

        MimeMessage msg = received[0];
        assertThat(msg.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(msg.getSubject()).isEqualTo("Тест тема");
        assertThat(msg.getContent().toString()).contains("Здравствуйте! Это тестовое письмо");
    }
}
