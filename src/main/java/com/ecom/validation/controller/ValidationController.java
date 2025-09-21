package com.ecom.validation.controller;

import com.ecom.validation.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping(path = "/validation-newSend")
    public ResponseEntity<?> newSendCode(@RequestBody Map<String, Long> id) {
        Long validationId = id.get("id");
        return ResponseEntity.ok().body(this.validationService.sendNewCode(validationId));
    }

}
