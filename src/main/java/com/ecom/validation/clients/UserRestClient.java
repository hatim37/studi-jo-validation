package com.ecom.validation.clients;

import com.ecom.validation.dto.ActivPasswordDto;
import com.ecom.validation.dto.LoginActivationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "users-service", url = "${users.service.url}")
public interface UserRestClient {

    @PostMapping("/_internal/user-activation")
    @CircuitBreaker(name="validation", fallbackMethod = "getDefaultActivation")
    ResponseEntity<Void> activationUsers(@RequestHeader("Authorization") String authorization, @RequestBody LoginActivationDto loginDto);

    @PostMapping("/_internal/new-password")
    @CircuitBreaker(name="password", fallbackMethod = "getDefaultActivPassword")
    ResponseEntity<Void> activationPassword(@RequestHeader("Authorization") String authorization,@RequestBody ActivPasswordDto activPasswordDto);

    default ResponseEntity<Void> getDefaultActivation(String authorization,LoginActivationDto loginActivationDto, Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    default ResponseEntity<Void> getDefaultActivPassword(String authorization, ActivPasswordDto activPasswordDto, Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

}
