package com.app.authservice.service;


import com.app.authservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendVerificationEmail(User user, String token, String verificationUrl) {
        String subject = "Verify your email";
        String body = "Click the link to verify your account: " + verificationUrl;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

}

