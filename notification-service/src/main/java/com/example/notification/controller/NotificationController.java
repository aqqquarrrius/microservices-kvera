package com.example.notification.controller;


import com.example.notification.dto.SendEmailRequest;
import com.example.notification.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendEmail(@RequestBody @Valid SendEmailRequest req) {
        emailService.sendPlain(req.getTo(), req.getSubject(), req.getBody());
        return ResponseEntity.accepted().build();
    }
}