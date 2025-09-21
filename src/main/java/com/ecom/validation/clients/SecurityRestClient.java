package com.ecom.validation.clients;

import com.ecom.validation.dto.LoginActivationDto;
import com.ecom.validation.dto.TokenTechnicDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "security-service", url = "${security.service.url}")
public interface SecurityRestClient {

    @PostMapping(value="/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @CircuitBreaker(name="tokenTechnic", fallbackMethod = "getDefaultToken")
    TokenTechnicDto getTokenTechnic(@RequestHeader("Authorization") String authorization, @RequestBody String formBody);

    default TokenTechnicDto getDefaultToken(String authorization, String formBody, Exception e) {
        return new TokenTechnicDto(null, null, 0L, null);
    }

    @PostMapping("/_internal/login-activation-deviceId")
    @CircuitBreaker(name="validation", fallbackMethod = "getDefaultActivation")
    ResponseEntity<Void> activationLogin(@RequestHeader("Authorization") String authorization, @RequestBody LoginActivationDto loginDto);

    default ResponseEntity<Void> getDefaultActivation(String authorization, LoginActivationDto loginDto, Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

}
