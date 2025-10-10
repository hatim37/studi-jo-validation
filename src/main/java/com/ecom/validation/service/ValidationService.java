package com.ecom.validation.service;

import com.ecom.validation.dto.ValidationDto;
import com.ecom.validation.entity.Validation;
import com.ecom.validation.repository.ValidationRepository;
import com.ecom.validation.response.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Transactional
@Service
@Slf4j
public class ValidationService {

    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    private ValidationRepository validationRepository;
    private NotificationService notificationService;

    public ResponseEntity<Validation> save(ValidationDto validationDto){
        //je recherche si le code existe et non activé, je le supprime
        if (validationDto.getDeviceId() != null) {
            this.validationRepository.deleteByUserIdAndDeviceIdAndActiveFalse(validationDto.getUserId(), validationDto.getDeviceId());
        }
        //je construis mon Object
        Validation validation = new Validation();
        //on ajoute le user
        validation.setUserId(validationDto.getUserId());
        //on ajoute la date de création
        Instant creatAt = Instant.now();
        validation.setCreatAt(creatAt);
        // on ajoute la durée de validation
        Instant ExpireAt = creatAt.plus(10, ChronoUnit.MINUTES);
        validation.setExpireAt(ExpireAt);
        // on créer un code de validation
        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);
        validation.setCode(code);
        //on ajoute le email
        validation.setEmail(validationDto.getEmail());
        //on ajoute le username
        validation.setUsername(validationDto.getUsername());
        //on ajoute le type
        validation.setType(validationDto.getType());
        //on ajoute le deviceId si présent
        if (validationDto.getDeviceId() != null) {
            validation.setDeviceId(validationDto.getDeviceId());
        }
        //on sauvegarde
        this.validationRepository.save(validation);
        //on envoi un mail
        this.notificationService.send(validationDto,code);
        return ResponseEntity.ok(validation);
    }



    @Scheduled(cron= "@weekly")
    public void removeCode() {
        final Instant now = Instant.now();
        this.validationRepository.deleteAllByExpireAtBefore(now);
    }

    public ResponseEntity<?> sendNewCode(Long id) {
        //on recherche l'utilisateur
        Validation validation = this.validationRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Utilisateur introuvable"));
        //on vérifie si le compte n'est pas actif
        if (!validation.getActive()){
            //on change le code
            Random random = new Random();
            int randomInteger = random.nextInt(999999);
            String code = String.format("%06d", randomInteger);
            validation.setCode(code);
            //on change la date de création
            Instant creatAt = Instant.now();
            validation.setCreatAt(creatAt);
            // on change la durée de validation
            Instant ExpireAt = creatAt.plus(10, ChronoUnit.MINUTES);
            validation.setExpireAt(ExpireAt);
            //on sauvegarde
            validationRepository.save(validation);
            //on renvoie un mail
            this.notificationService.send(new ValidationDto(validation.getUserId(), validation.getUsername(), validation.getDeviceId(), validation.getEmail(), validation.getType()), code);
            return ResponseEntity.status(HttpStatus.CREATED).body("Votre code a été envoyé");
        } else {
            return ResponseEntity.ok("Le code a déjà été validé");
        }
    }
}
