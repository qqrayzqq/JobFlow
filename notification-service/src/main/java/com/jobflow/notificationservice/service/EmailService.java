package com.jobflow.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendApplicationConfirmation(String to, String jobTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@jobflow.com");
        message.setTo(to);
        message.setSubject("Application recieved");
        message.setText("You applied to: " + jobTitle);
        mailSender.send(message);
    }
}
