package com.ecom.validation.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;


@Configuration
@Profile({"docker","local"})
public class MailConfigDocker {

    //simulation injection des dependances pour JavaMailSender avec Docker
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {}

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {}

            @Override
            public MimeMessage createMimeMessage() {return new MimeMessage((Session) null);}

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage(null, contentStream);
                } catch (Exception e) {
                    return new MimeMessage((Session) null);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {}

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {}

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {}

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {}
        };
    }
}
