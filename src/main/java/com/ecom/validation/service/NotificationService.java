package com.ecom.validation.service;

import com.ecom.validation.dto.ValidationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Slf4j
@AllArgsConstructor
@Service
public class NotificationService {


    JavaMailSender javaMailSender;

    public void send(ValidationDto validationDto, String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@hatim.tech");
        message.setTo(validationDto.getEmail());
        String username = validationDto.getUsername();


        if (validationDto.getType().equals("registration")) {
            message.setSubject("Votre code d'activation");
            String texte = String.format("Bonjour "+username+","+"\n\nVotre code d'activation est "+code+".\n\nA bientôt.");
            message.setText(texte);
            log.info(texte);
        }
        if (validationDto.getType().equals("editPassword")) {
            message.setSubject("Réinitialiser votre mot de passe");
            String texte = String.format("Bonjour "+username+","+"\n\nVotre code de réinitialisation est "+code+".\n\nA bientôt.");
            message.setText(texte);
            log.info(texte);
        }
        if (validationDto.getType().equals("deviceId")) {
            message.setSubject("Votre code de connexion");
            String texte = String.format("Bonjour "+username+","+"\n\nVotre code de connexion est "+code+".\n\nA bientôt.");
            message.setText(texte);
            log.info(texte);
        }

        javaMailSender.send(message);

    }
}
