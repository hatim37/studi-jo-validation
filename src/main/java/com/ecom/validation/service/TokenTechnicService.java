package com.ecom.validation.service;



import com.ecom.validation.clients.SecurityRestClient;
import com.ecom.validation.dto.LoginActivationDto;
import com.ecom.validation.dto.TokenTechnicDto;
import com.ecom.validation.response.UserNotFoundException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class TokenTechnicService {



    private final SecurityRestClient securityRestClient;
    @Value("${CLIENT_ID}")
    private  String clientId;
    @Value("${CLIENT_SECRET}")
    private  String clientSecret;

    public TokenTechnicService(SecurityRestClient securityRestClient) {
        this.securityRestClient = securityRestClient;
    }


    public String getTechnicalToken() {
        // 1) Calcul du header Basic
        String creds = clientId+":"+clientSecret;
        String basicAuth = "Basic " +
                Base64.getEncoder().encodeToString(
                        creds.getBytes(StandardCharsets.UTF_8)
                );

        // 2) Construction manuelle du corps form-url-encoded

        String form = "grant_type=client_credentials&scope=users:read";

        // 3) Appel Feign
        //TokenTechnicDto resp = securityRestClient.getTokenTechnic(basicAuth, form);


        // 4) Retourne l’access_token (ou null si fallback)
        //return resp == null ? null : resp.accessToken();

        try {
            TokenTechnicDto resp = securityRestClient.getTokenTechnic(basicAuth, form);
            return resp == null ? null : resp.accessToken();
        } catch (FeignException e) {
            throw new UserNotFoundException("Service Token indisponible");
        }
    }


}
