package com.ecom.validation.service;

import com.ecom.validation.clients.SecurityRestClient;
import com.ecom.validation.clients.UserRestClient;
import com.ecom.validation.dto.ActivPasswordDto;
import com.ecom.validation.dto.LoginActivationDto;
import com.ecom.validation.entity.Validation;
import com.ecom.validation.repository.ValidationRepository;
import com.ecom.validation.response.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Slf4j
@Service
@Transactional
public class ActivationService {

    private final ValidationRepository validationRepository;
    private final UserRestClient userRestUser;
    private final TokenTechnicService tokenTechnicService;
    private final SecurityRestClient securityRestClient;

    public ActivationService(UserRestClient userRestUser, ValidationRepository validationRepository, TokenTechnicService tokenTechnicService, SecurityRestClient securityRestClient) {
        this.userRestUser = userRestUser;
        this.validationRepository = validationRepository;
        this.tokenTechnicService = tokenTechnicService;
        this.securityRestClient = securityRestClient;
    }

    public ResponseEntity<?> activation(String code, String password) {
        Validation validation = this.readCode(code);
        //on vérifie la date du code
        if(Instant.now().isAfter(validation.getExpireAt())) {
            throw new UserNotFoundException("Votre code est expiré");
        }
        //on vérifie si active
        if (validation.getActive()){
            throw new UserNotFoundException("Votre code a déja été activé");
        }
        //on active le code
        validation.setActive(true);
        validationRepository.save(validation);

        Validation controlValid  = this.validationRepository.findById(validation.getId()).orElse(null);
        //on envoi la requete au microservice
        assert controlValid != null;

       if(controlValid.getType().contains("registration")){
            ResponseEntity<Void> resp = this.userRestUser.activationUsers("Bearer "+this.tokenTechnicService.getTechnicalToken(),new LoginActivationDto(validation.getUserId(), null, validation.getActive()));
            if (resp.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Votre est compte est activé !");
            } else {throw new UserNotFoundException("Service indisponible");}
        }

        if(controlValid.getType().contains("deviceId")){
            ResponseEntity<Void> resp = this.securityRestClient.activationLogin("Bearer "+this.tokenTechnicService.getTechnicalToken(),new LoginActivationDto(validation.getUserId(), validation.getDeviceId(), validation.getActive()));
            if (resp.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Votre appareil est validé !");
            } else {
                throw new UserNotFoundException("Service indisponible");
            }
        }

        if(controlValid.getType().contains("editPassword")){
            ResponseEntity<Void> resp = this.userRestUser.activationPassword("Bearer "+this.tokenTechnicService.getTechnicalToken(),new ActivPasswordDto(validation.getUserId(), password));
            if (resp.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Votre mot de passe a été modifié avec succès !");
            } else {
                throw new UserNotFoundException("Service indisponible");
            }
        }

        else {
            throw new UserNotFoundException("Erreur, veuillez réessayer");
        }
    }

    //lire le code de validation
    public Validation readCode(String code){
        return this.validationRepository.findByCode(code).orElseThrow(() -> new UserNotFoundException("Votre code est invalide"));
    }
}
