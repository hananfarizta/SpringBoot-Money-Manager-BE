package dev.hananfarizta.moneymanager.controller;

import dev.hananfarizta.moneymanager.dto.AuthDTO;
import dev.hananfarizta.moneymanager.dto.ProfileDTO;
import dev.hananfarizta.moneymanager.exception.custom.AuthenticationException;
import dev.hananfarizta.moneymanager.exception.custom.BusinessException;
import dev.hananfarizta.moneymanager.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileControllerTest {

        private MockMvc mockMvc;
        private ProfileService profileService;
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                profileService = Mockito.mock(ProfileService.class);

                ProfileController controller = new ProfileController(profileService);

                objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(controller)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build();
        }

        private ProfileDTO sampleProfileDTO() {
                return ProfileDTO.builder()
                                .fullName("John Doe")
                                .email("john.doe@example.com")
                                .password("PlainPass123")
                                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        @Test
        @DisplayName("POST /auth/register → 201 dengan payload ApiResponseDTO berisi user + activationToken")
        void register_success() throws Exception {
                ProfileDTO req = sampleProfileDTO();

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("user", ProfileDTO.builder()
                                .fullName("John Doe")
                                .email("john.doe@example.com")
                                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                                .createdAt(req.getCreatedAt())
                                .updatedAt(req.getUpdatedAt())
                                .build());
                data.put("activationToken", "token-123");

                when(profileService.registerProfile(any(ProfileDTO.class))).thenReturn(data);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status", is("success")))
                                .andExpect(jsonPath("$.message", is("User created successfully")))
                                .andExpect(jsonPath("$.data.activationToken", is("token-123")))
                                .andExpect(jsonPath("$.data.user.email", is("john.doe@example.com")));
        }

        @Test
        @DisplayName("GET /auth/activate → 200 jika service mengembalikan true")
        void activate_success() throws Exception {
                when(profileService.activateProfile("abc")).thenReturn(true);

                mockMvc.perform(get("/auth/activate").param("token", "abc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("success")))
                                .andExpect(jsonPath("$.message", is("Profile activated successfully")));
        }

        @Test
        @DisplayName("GET /auth/activate → 404 jika service mengembalikan false")
        void activate_notFound() throws Exception {
                when(profileService.activateProfile("missing")).thenReturn(false);

                mockMvc.perform(get("/auth/activate").param("token", "missing"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status", is("error")))
                                .andExpect(jsonPath("$.message",
                                                is("Activation token not found or already activated")));
        }

        @Test
        @DisplayName("POST /auth/login → 200 dengan token dan user")
        void login_success() throws Exception {
                AuthDTO req = AuthDTO.builder()
                                .email("john.doe@example.com")
                                .password("PlainPass123")
                                .build();

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("user", ProfileDTO.builder()
                                .fullName("John Doe")
                                .email("john.doe@example.com")
                                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());
                data.put("token", "jwt-123");

                when(profileService.authenticateAndGenerateToken(any(AuthDTO.class))).thenReturn(data);

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("success")))
                                .andExpect(jsonPath("$.message", is("User authenticated successfully")))
                                .andExpect(jsonPath("$.data.token", is("jwt-123")))
                                .andExpect(jsonPath("$.data.user.email", is("john.doe@example.com")));
        }

        @Test
        @DisplayName("@ExceptionHandler BusinessException → 400")
        void businessException_handler() throws Exception {
                when(profileService.registerProfile(any(ProfileDTO.class)))
                                .thenThrow(new BusinessException("any business error"));

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleProfileDTO())))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status", is("error")))
                                .andExpect(jsonPath("$.message", is("any business error")));
        }

        @Test
        @DisplayName("@ExceptionHandler AuthenticationException → 401")
        void authenticationException_handler() throws Exception {
                when(profileService.authenticateAndGenerateToken(any(AuthDTO.class)))
                                .thenThrow(new AuthenticationException("unauthorized") {
                                });

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(AuthDTO.builder()
                                                .email("john.doe@example.com").password("x").build())))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status", is("error")))
                                .andExpect(jsonPath("$.message", is("unauthorized")));
        }
}
