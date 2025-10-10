package com.ecom.validation.service;

import com.ecom.validation.dto.ValidationDto;
import com.ecom.validation.entity.Validation;
import com.ecom.validation.repository.ValidationRepository;
import com.ecom.validation.response.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ValidationServiceIntegrationTest {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ValidationRepository validationRepository;

    @MockBean
    private NotificationService notificationService;

    private ValidationDto validationDto;

    @BeforeEach
    void setUp() {
        // Création d’un objet DTO
        validationDto = new ValidationDto();
        validationDto.setUserId(1L);
        validationDto.setEmail("test@mail.com");
        validationDto.setUsername("john");
        validationDto.setType("registration");
    }

    // 1 : Création de validation réussie
    @Test
    void testCreateValidationSuccess() {
        // Appel de la méthode
        ResponseEntity<Validation> response = validationService.save(validationDto);

        // Vérification, statut 200, code généré, inactif par défaut
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Validation saved = response.getBody();
        assertThat(saved).isNotNull();
        assertThat(saved.getCode()).isNotEmpty();
        assertThat(saved.getActive()).isFalse();

        // Vérifie envoi email
        verify(notificationService, times(1)).send(any(), anyString());
    }

    // 2 : Création avec un deviceId
    @Test
    void testCreateValidationWithDeviceId() {
        validationDto.setDeviceId(100L);

        Validation val = new Validation();
        val.setUserId(1L);
        val.setDeviceId(100L);
        val.setActive(false);
        validationRepository.save(val);

        // Appel du service, suppression ancien deviceId
        ResponseEntity<Validation> response = validationService.save(validationDto);

        // Vérification, nouveau code sauvegardé
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        // Vérification, une seule entrée restante
        assertThat(validationRepository.findAll()).hasSize(1);

        // Vérification, envoi email
        verify(notificationService, times(1)).send(any(), anyString());
    }

    // 3 : Suppression automatique des validations expirées
    @Test
    void testRemoveExpiredValidations() {
        // Insertion d’une validation expirée
        Validation expired = new Validation();
        expired.setUserId(1L);
        expired.setEmail("old@mail.com");
        expired.setExpireAt(Instant.now().minusSeconds(60));
        validationRepository.save(expired);

        // Appel de la méthode planifiée manuellement
        validationService.removeCode();

        // Vérification, BDD vide après suppression
        assertThat(validationRepository.findAll()).isEmpty();
    }

    // 4 : Renvoi d’un nouveau code = succès
    @Test
    void testSendNewCodeSuccess() {
        // Création d’un enregistrement inactif
        Validation validation = new Validation();
        validation.setUserId(1L);
        validation.setEmail("test@mail.com");
        validation.setActive(false);
        validationRepository.save(validation);

        // Appel du service pour renvoyer un nouveau code
        ResponseEntity<?> response = validationService.sendNewCode(validation.getId());

        // Vérification, création réussie
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo("Votre code a été envoyé");

        // Vérification, email envoyé
        verify(notificationService, times(1)).send(any(), anyString());
    }

    // 5 : Renvoi d’un code déjà validé
    @Test
    void testSendNewCodeAlreadyValidated() {
        // Création d’un enregistrement déjà validé
        Validation validation = new Validation();
        validation.setUserId(1L);
        validation.setEmail("test@mail.com");
        validation.setActive(true);
        validationRepository.save(validation);

        // Appel de la méthode
        ResponseEntity<?> response = validationService.sendNewCode(validation.getId());

        // Vérification, aucun nouvel envoi n’est fait
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Le code a déjà été validé");
    }

    // 6 : Renvoi d’un code pour un utilisateur inexistant
    @Test
    void testSendNewCodeUserNotFound() {
        // Vérification, exception UserNotFoundException avec message
        assertThatThrownBy(() -> validationService.sendNewCode(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }
}