package dev.hananfarizta.moneymanager.controller;

import dev.hananfarizta.moneymanager.dto.AuthDTO;
import dev.hananfarizta.moneymanager.dto.ProfileDTO;
import dev.hananfarizta.moneymanager.exception.custom.AuthenticationException;
import dev.hananfarizta.moneymanager.exception.custom.BusinessException;
import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;
import dev.hananfarizta.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> registerProfile(@RequestBody ProfileDTO profileDTO) {
        Map<String, Object> registeredProfile = profileService.registerProfile(profileDTO);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "User created successfully",
                registeredProfile);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponseDTO<String>> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        ApiResponseDTO<String> response;
        if (isActivated) {
            response = new ApiResponseDTO<>("success", "Profile activated successfully", null);
            return ResponseEntity.ok(response);
        } else {
            response = new ApiResponseDTO<>("error", "Activation token not found or already activated", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> login(@RequestBody AuthDTO authDTO) {
        Map<String, Object> data = profileService.authenticateAndGenerateToken(authDTO);
        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "User authenticated successfully",
                data);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBusinessError(BusinessException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>("error", ex.getMessage(), null);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAuthError(AuthenticationException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>("error", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
