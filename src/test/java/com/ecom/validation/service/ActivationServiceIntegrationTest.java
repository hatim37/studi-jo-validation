package com.ecom.validation.service;

import com.ecom.validation.clients.SecurityRestClient;
import com.ecom.validation.clients.UserRestClient;
import com.ecom.validation.dto.ActivPasswordDto;
import com.ecom.validation.dto.LoginActivationDto;
import com.ecom.validation.entity.Validation;
import com.ecom.validation.repository.ValidationRepository;
import com.ecom.validation.response.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActivationServiceIntegrationTest {

    @Autowired
    private ActivationService activationService;

    @Autowired
    private ValidationRepository validationRepository;

    @MockBean
    private UserRestClient userRestClient;

    @MockBean
    private SecurityRestClient securityRestClient;

    @MockBean
    private TokenTechnicService tokenTechnicService;

    private Validation validation;

    @BeforeEach
    void setUp() {
        validationRepository.deleteAll();

        // Création validation
        validation = new Validation();
        validation.setUserId(1L);
        validation.setEmail("user@mail.com");
        validation.setActive(false);
        validation.setCode("123456");
        validation.setType("registration");
        validation.setCreatAt(Instant.now());
        validation.setExpireAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        validationRepository.save(validation);

        // Simulation d’un token technique valide
        when(tokenTechnicService.getTechnicalToken()).thenReturn("mocked-token");
    }

    // 1 : Activation réussie pour inscription
    @Test
    void testActivationSuccess_Registration() {
        // On simule la réponse du microservice Users
        when(userRestClient.activationUsers(anyString(), any()))
                .thenReturn(ResponseEntity.ok().build());

        // Appel du service d’activation
        ResponseEntity<?> response = activationService.activation("123456", "newPass123");

        // Vérification, status HTTP, body message
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Votre est compte est activé !");

        // Vérification, active = true
        Validation updated = validationRepository.findByCode("123456").orElseThrow();
        assertThat(updated.getActive()).isTrue();
        //Vérification, envoi validation au service Users
        verify(userRestClient, times(1))
                .activationUsers(eq("Bearer mocked-token"), any(LoginActivationDto.class));
    }

    // 2️ : Code expiré = erreur
    @Test
    void testActivation_CodeExpired() {
        // On rend le code expiré
        validation.setExpireAt(Instant.now().minusSeconds(60));
        validationRepository.save(validation);

        // Vérification, exception levée avec message
        assertThatThrownBy(() -> activationService.activation("123456", "newPass123"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Votre code est expiré");
    }

    // 3 : Code déjà activé = erreur
    @Test
    void testActivation_CodeAlreadyUsed() {
        validation.setActive(true);
        validationRepository.save(validation);
        // Vérification, exception levée avec message
        assertThatThrownBy(() -> activationService.activation("123456", "newPass123"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Votre code a déja été activé");
    }

    // 4️ : Activation d’un appareil (deviceId)
    @Test
    void testActivation_DeviceId() {
        validation.setType("deviceId");
        validationRepository.save(validation);

        when(securityRestClient.activationLogin(anyString(), any()))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = activationService.activation("123456", null);

        // vérification, status HTTP, body avec message
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Votre appareil est validé !");
        verify(securityRestClient, times(1))
                .activationLogin(eq("Bearer mocked-token"), any(LoginActivationDto.class));
    }

    // 5 : Modification du mot de passe
    @Test
    void testActivation_EditPassword() {
        validation.setType("editPassword");
        validationRepository.save(validation);

        when(userRestClient.activationPassword(anyString(), any()))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = activationService.activation("123456", "newPass999");

        // Vérification, status HTTP, body avec message
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Votre mot de passe a été modifié avec succès !");
        verify(userRestClient, times(1))
                .activationPassword(eq("Bearer mocked-token"), any(ActivPasswordDto.class));
    }

    // 6 : Code invalide & utilisateur introuvable
    @Test
    void testActivation_InvalidCode() {
        // Vérification, exception levée avec message
        assertThatThrownBy(() -> activationService.activation("000000", "password"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Votre code est invalide");
    }
}
