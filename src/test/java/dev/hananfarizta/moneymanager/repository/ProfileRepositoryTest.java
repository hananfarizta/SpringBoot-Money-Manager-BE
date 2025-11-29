package dev.hananfarizta.moneymanager.repository;

import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    private ProfileEntity buildProfile(String email, String token, Boolean isActive) {
        return ProfileEntity.builder()
                .fullName("John Doe")
                .email(email)
                .password("$2a$10$encoded")
                .profileImageUrl("https://cdn.example.com/avatars/1.png")
                .isActive(isActive)
                .activationToken(token)
                .build();
    }

    @Test
    @DisplayName("findByEmail mengembalikan entity sesuai email")
    void findByEmail_success() {
        ProfileEntity saved = profileRepository.save(buildProfile("john.doe@example.com", "token-123", true));

        Optional<ProfileEntity> found = profileRepository.findByEmail("john.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("findByActivationToken mengembalikan entity sesuai token")
    void findByActivationToken_success() {
        ProfileEntity saved = profileRepository.save(buildProfile("jane.doe@example.com", "abc-xyz", false));

        Optional<ProfileEntity> found = profileRepository.findByActivationToken("abc-xyz");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.get().getActivationToken()).isEqualTo(saved.getActivationToken());
    }

    @Test
    @DisplayName("existsByEmail true jika email sudah ada")
    void existsByEmail_true() {
        profileRepository.save(buildProfile("exists@example.com", "tkn", false));
        assertThat(profileRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(profileRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    @DisplayName("@PrePersist menetapkan default isActive=false jika null")
    void prePersist_default_isActive_false() {
        ProfileEntity entity = buildProfile("default@example.com", "tkn", null);
        ProfileEntity saved = profileRepository.save(entity);

        assertThat(saved.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("@CreationTimestamp dan @UpdateTimestamp terisi otomatis")
    void timestamps_are_populated() {
        ProfileEntity saved = profileRepository.save(buildProfile("stamp@example.com", "tkn", true));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        saved.setFullName("John X");
        ProfileEntity updated = profileRepository.save(saved);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
    }

    @Test
    @DisplayName("Constraint unik email → menyimpan email duplikat melempar exception")
    void unique_email_constraint() {
        profileRepository.save(buildProfile("unique@example.com", "tkn1", false));

        ProfileEntity duplicate = buildProfile("unique@example.com", "tkn2", true);

        assertThatThrownBy(() -> profileRepository.saveAndFlush(duplicate))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
