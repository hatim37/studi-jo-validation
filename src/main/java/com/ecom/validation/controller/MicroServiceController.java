package com.ecom.validation.controller;

import com.ecom.validation.dto.ValidationDto;
import com.ecom.validation.entity.Validation;
import com.ecom.validation.service.TokenTechnicService;
import com.ecom.validation.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MicroServiceController {


    private final ValidationService validationService;
    private TokenTechnicService tokenTechnicService;

    public MicroServiceController(ValidationService validationService, TokenTechnicService tokenTechnicService) {
        this.validationService = validationService;
        this.tokenTechnicService = tokenTechnicService;
    }

    @PostMapping(path = "/_internal/validation-send")
    public ResponseEntity<Validation> sendCode(@RequestBody ValidationDto validationDto) {
        return this.validationService.save(validationDto);
    }


    @GetMapping(path = "/tokenTechnic")
    public String tokken() {
        return this.tokenTechnicService.getTechnicalToken();
    }
}
