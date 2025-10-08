package com.ecom.validation.service;

import com.ecom.validation.dto.ValidationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationService {


    JavaMailSender javaMailSender;
    private final Environment environment;

    public void send(ValidationDto validationDto, String code){

        String username = validationDto.getUsername();
        String texte = "";
        String subject = "";

        if (validationDto.getType().equals("registration")) {
            subject = "Votre code d'activation";
            texte = String.format("Bonjour "+username+","+"\n\nVotre code d'activation est "+code+".\n\nA bientôt.");
        }
        if (validationDto.getType().equals("editPassword")) {
            subject ="Réinitialiser votre mot de passe";
            texte = String.format("Bonjour "+username+","+"\n\nVotre code de réinitialisation est "+code+".\n\nA bientôt.");
        }
        if (validationDto.getType().equals("deviceId")) {
            subject ="Votre code de connexion";
            texte = String.format("Bonjour "+username+","+"\n\nVotre code de connexion est "+code+".\n\nA bientôt.");
        }
        log.info("VOTRE MAIL CONTIENT [{}] -> {}", subject, texte);

        // Vérifie si le profil actif est "local"
        if (Arrays.asList(environment.getActiveProfiles()).contains("cloud")) {

            // Envoi réel
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@hatim.tech");
            message.setTo(validationDto.getEmail());
            message.setSubject(subject);
            message.setText(texte);
            javaMailSender.send(message);
        }

    }
}
