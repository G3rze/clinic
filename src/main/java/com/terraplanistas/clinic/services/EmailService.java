package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.http.config.email.SmtpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final MailSender mailSender;
    private final SmtpProperties properties;

    public EmailService(MailSender mailSender, SmtpProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendTestEmail(String to) {
        if (!properties.isEnabled()) {
            log.info("Email sending is disabled. Skipping test email to {}", to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getDefaultFrom());
        message.setTo(to);
        message.setSubject("Clinic System - Test Email");
        message.setText("This is a test email from the Clinic appointment system.");
        mailSender.send(message);
        log.info("Test email sent to {}", to);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendStartupTestEmail() {
        if (!properties.isEnabled()) {
            log.info("Email sending is disabled. Skipping startup test email.");
            return;
        }
        try {
            sendTestEmail(properties.getDefaultFrom());
        } catch (Exception e) {
            log.error("Failed to send startup test email", e);
        }
    }
}