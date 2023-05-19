package it.gov.innovazione.ndc.controller;

import it.gov.innovazione.ndc.validator.ValidationResultDto;
import it.gov.innovazione.ndc.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<ValidationResultDto> validateFile(@RequestParam(value = "type", required = true) String assetType, @RequestParam(value = "file", required = true) MultipartFile file) {
        return AppJsonResponse.ok(validationService.validate(file, assetType));

    }
}
