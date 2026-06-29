package com.terraplanistas.clinic.http.config.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class SmtpConfig {

    private final SmtpProperties properties;

    public SmtpConfig(SmtpProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MailSender mailSender() {
        if (!properties.isEnabled()) {
            return new NoOpMailSender();
        }

        return switch (properties.getProvider().getType()) {
            case "smtp" -> createSmtpSender();
            default -> throw new IllegalStateException("Unknown email provider: " +
                    properties.getProvider().getType());
        };
    }

    private MailSender createSmtpSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        SmtpProperties.Smtp smtp = properties.getProvider().getSmtp();
        sender.setHost(smtp.getHost());
        sender.setPort(smtp.getPort());
        sender.setUsername(smtp.getUsername());
        sender.setPassword(smtp.getPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(smtp.isStarttls()));
        return sender;
    }

    private static class NoOpMailSender implements MailSender {
        @Override
        public void send(SimpleMailMessage simpleMessage) {
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
        }
    }
}