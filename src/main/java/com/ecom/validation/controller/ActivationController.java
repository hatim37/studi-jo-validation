package com.ecom.validation.controller;

import com.ecom.validation.service.ActivationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@Slf4j
@AllArgsConstructor
@RestController
public class ActivationController {

    private ActivationService activationService;

    @PostMapping(path = "/activation-send")
    public ResponseEntity<?> validCode(@RequestBody Map<String, String> code) {
        String activationCode = code.get("code");
        String password = code.get("password");
        log.info("code recu :"+activationCode);
        return ResponseEntity.ok().body(this.activationService.activation(activationCode, password));
    }

}
