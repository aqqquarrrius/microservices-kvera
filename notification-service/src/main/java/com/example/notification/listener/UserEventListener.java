package com.example.notification.listener;

import com.example.notification.kafka.UserEvent;
import com.example.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final EmailService emailService;

    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.user-events-topic}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userEventKafkaListenerContainerFactory")

    public void onUserEvent(UserEvent event) {
        if (event == null || event.getEmail() == null) return;

        switch (event.getOperation()) {
            case CREATE -> emailService.sendPlain(
                    event.getEmail(),
                    "Аккаунт создан",
                    "Здравствуйте! Ваш аккаунт на сайте был успешно создан"
            );
            case DELETE -> emailService.sendPlain(
                    event.getEmail(),
                    "Аккаунт удален",
                    "Здравствуйте! Ваш аккаунт был удален"
            );
        }
    }
}
