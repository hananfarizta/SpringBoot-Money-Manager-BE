package dev.hananfarizta.moneymanager.service;

import dev.hananfarizta.moneymanager.dto.AuthDTO;
import dev.hananfarizta.moneymanager.dto.ProfileDTO;
import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import dev.hananfarizta.moneymanager.repository.ProfileRepository;
import dev.hananfarizta.moneymanager.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    void setUp() throws Exception {
        var field = ProfileService.class.getDeclaredField("appActivationUrl");
        field.setAccessible(true);
        field.set(profileService, "https://app.example.com");
    }

    private ProfileDTO sampleProfileDTO() {
        return ProfileDTO.builder()
                .id(null)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("PlainPass123")
                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ProfileEntity sampleProfileEntity(String activationToken, boolean isActive) {
        return ProfileEntity.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("$2a$10$encoded")
                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activationToken(activationToken)
                .isActive(isActive)
                .build();
    }

    @Nested
    @DisplayName("registerProfile")
    class RegisterProfileTests {

        @Test
        @DisplayName("berhasil register: simpan user, kirim email, kembalikan token dan DTO user")
        void registerProfile_success() {
            ProfileDTO input = sampleProfileDTO();

            when(profileRepository.existsByEmail(input.getEmail())).thenReturn(false);
            when(passwordEncoder.encode("PlainPass123")).thenReturn("$2a$10$encoded");

            ArgumentCaptor<ProfileEntity> entityCaptor = ArgumentCaptor.forClass(ProfileEntity.class);

            when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(inv -> {
                ProfileEntity e = inv.getArgument(0);

                if (e.getActivationToken() == null) {
                    e.setActivationToken(UUID.randomUUID().toString());
                }
                e.setId(1L);
                return e;
            });

            Map<String, Object> result = profileService.registerProfile(input);

            verify(profileRepository).save(entityCaptor.capture());
            ProfileEntity saved = entityCaptor.getValue();
            assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(saved.getIsActive()).isFalse();
            assertThat(saved.getActivationToken()).isNotBlank();
            assertThat(saved.getPassword()).isEqualTo("$2a$10$encoded");

            verify(emailService, times(1)).sendEmail(
                    eq("john.doe@example.com"),
                    eq("Activate Your Account"),
                    contains("https://app.example.com/api/v1.0/activate?token=" + saved.getActivationToken()));

            assertThat(result).containsKeys("user", "activationToken");
            ProfileDTO userDto = (ProfileDTO) result.get("user");
            assertThat(userDto.getEmail()).isEqualTo("john.doe@example.com");
            assertThat((String) result.get("activationToken")).isEqualTo(saved.getActivationToken());
        }

        @Test
        @DisplayName("gagal: email sudah terdaftar")
        void registerProfile_emailExists() {
            ProfileDTO input = sampleProfileDTO();
            when(profileRepository.existsByEmail(input.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> profileService.registerProfile(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already registered");

            verify(profileRepository, never()).save(any());
            verify(emailService, never()).sendEmail(any(), any(), any());
        }

        @Test
        @DisplayName("gagal: validasi email format salah")
        void registerProfile_invalidEmail() {
            ProfileDTO input = ProfileDTO.builder()
                    .id(null)
                    .fullName("John Doe")
                    .email("invalid-email")
                    .password("PlainPass123")
                    .profileImageUrl("https://cdn.example.com/avatars/1.png")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            assertThatThrownBy(() -> profileService.registerProfile(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid email format");
        }

        @Test
        @DisplayName("gagal: full name kosong")
        void registerProfile_fullNameEmpty() {
            ProfileDTO input = ProfileDTO.builder()
                    .id(null)
                    .fullName(" ")
                    .email("johndoe@mail.com")
                    .password("PlainPass123")
                    .profileImageUrl("https://cdn.example.com/avatars/1.png")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            assertThatThrownBy(() -> profileService.registerProfile(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Full Name cannot be empty");
        }

        @Test
        @DisplayName("gagal: password kosong")
        void registerProfile_passwordEmpty() {
            ProfileDTO input = ProfileDTO.builder()
                    .id(null)
                    .fullName("John Doe")
                    .email("johndoe@mail.com")
                    .password(" ")
                    .profileImageUrl("https://cdn.example.com/avatars/1.png")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            assertThatThrownBy(() -> profileService.registerProfile(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be empty");
        }
    }

    @Nested
    @DisplayName("activateProfile")
    class ActivateProfileTests {

        @Test
        @DisplayName("token valid → aktifkan dan return true")
        void activateProfile_success() {
            String token = UUID.randomUUID().toString();
            ProfileEntity entity = sampleProfileEntity(token, false);

            when(profileRepository.findByActivationToken(token)).thenReturn(Optional.of(entity));
            when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            boolean res = profileService.activateProfile(token);

            assertThat(res).isTrue();
            assertThat(entity.getIsActive()).isTrue();
            verify(profileRepository).save(entity);
        }

        @Test
        @DisplayName("token tidak ditemukan → false")
        void activateProfile_notFound() {
            String token = UUID.randomUUID().toString();
            when(profileRepository.findByActivationToken(token)).thenReturn(Optional.empty());

            boolean res = profileService.activateProfile(token);

            assertThat(res).isFalse();
            verify(profileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("authenticateAndGenerateToken")
    class AuthenticateTests {

        @Test
        @DisplayName("berhasil login: user aktif, auth ok, generate token")
        void authenticate_success() {
            AuthDTO auth = AuthDTO.builder()
                    .email("john.doe@example.com")
                    .password("PlainPass123")
                    .build();

            ProfileEntity entity = sampleProfileEntity(UUID.randomUUID().toString(), true);

            when(profileRepository.findByEmail(auth.getEmail())).thenReturn(Optional.of(entity));

            var authResult = new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword());
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authResult);

            when(jwtUtil.generateToken(auth.getEmail())).thenReturn("jwt-token-123");

            Map<String, Object> res = profileService.authenticateAndGenerateToken(auth);

            assertThat(res).containsKeys("user", "token");
            assertThat((String) res.get("token")).isEqualTo("jwt-token-123");
            ProfileDTO dto = (ProfileDTO) res.get("user");
            assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("gagal: email belum terdaftar")
        void authenticate_emailNotRegistered() {
            AuthDTO auth = AuthDTO.builder()
                    .email("unknown@example.com")
                    .password("pass")
                    .build();

            when(profileRepository.findByEmail(auth.getEmail())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(auth))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email is not registered");
        }

        @Test
        @DisplayName("gagal: akun belum aktif")
        void authenticate_notActive() {
            AuthDTO auth = AuthDTO.builder()
                    .email("john.doe@example.com")
                    .password("pass")
                    .build();

            ProfileEntity entity = sampleProfileEntity(UUID.randomUUID().toString(), false);
            when(profileRepository.findByEmail(auth.getEmail())).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(auth))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Account is not activated. Please check your email.");
        }

        @Test
        @DisplayName("gagal: kredensial salah (BadCredentialsException)")
        void authenticate_badCredentials() {
            AuthDTO auth = AuthDTO.builder()
                    .email("john.doe@example.com")
                    .password("wrong")
                    .build();

            ProfileEntity entity = sampleProfileEntity(UUID.randomUUID().toString(), true);
            when(profileRepository.findByEmail(auth.getEmail())).thenReturn(Optional.of(entity));
            doThrow(new BadCredentialsException("Invalid email or password"))
                    .when(authenticationManager).authenticate(
                            new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));

            assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(auth))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("gagal: validasi input email format salah")
        void authenticate_invalidEmailFormat() {
            AuthDTO auth = AuthDTO.builder().email("invalid").password("x").build();

            assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(auth))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid email format");
        }

        @Test
        @DisplayName("gagal: password kosong")
        void authenticate_passwordEmpty() {
            AuthDTO auth = AuthDTO.builder().email("a@b.com").password(" ").build();

            assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(auth))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be empty");
        }
    }
}
