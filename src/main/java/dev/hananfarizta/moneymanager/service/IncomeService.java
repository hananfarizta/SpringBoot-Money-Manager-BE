package dev.hananfarizta.moneymanager.service;

import dev.hananfarizta.moneymanager.dto.IncomeDTO;
import dev.hananfarizta.moneymanager.entity.CategoryEntity;
import dev.hananfarizta.moneymanager.entity.IncomeEntity;
import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import dev.hananfarizta.moneymanager.repository.CategoryRepository;
import dev.hananfarizta.moneymanager.repository.IncomeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final IncomeRepository incomeRepository;

    // Add Income
    public Map<String, Object> addIncome(IncomeDTO incomeDTO) {
        validateAddedIncome(incomeDTO);

        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            CategoryEntity categoryEntity = categoryRepository.findById(incomeDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            IncomeEntity newIncome = toEntity(incomeDTO, profileEntity, categoryEntity);
            newIncome = incomeRepository.save(newIncome);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("income", toDTO(newIncome));

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add income", e);
        }
    }

    public void validateAddedIncome(IncomeDTO incomeDTO) {
        if (incomeDTO == null) {
            throw new IllegalArgumentException("income data cannot be null");
        }

        if (incomeDTO.getName() == null || incomeDTO.getName().isBlank()) {
            throw new IllegalArgumentException("income name cannot be empty");
        }

        if (incomeDTO.getAmount() == null || incomeDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("income amount must be greater than zero");
        }

        if (incomeDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("income must have a valid category");
        }
    }

    // helper methods
    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profileEntity)
                .category(categoryEntity)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryname(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : "N/A")
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }

    // Retrieve Incomes for The Current month/based on the start date and end date
    public Map<String, Object> getCurrentMonthIncomesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            LocalDate now = LocalDate.now();
            LocalDate startDate = now.withDayOfMonth(1);
            LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

            List<IncomeDTO> incomeDTOs = incomeRepository
                    .findByProfileIdAndDateBetween(profileEntity.getId(),
                            startDate, endDate)
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("incomes", incomeDTOs);

            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Incomes", e);
        }
    }

    // Delete Income by Id for Current User
    public void deleteIncome(Long incomeId) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            IncomeEntity incomeEntity = incomeRepository.findById(
                    incomeId)
                    .orElseThrow(() -> new IllegalArgumentException("Income not found"));

            if (!incomeEntity.getProfile().getId().equals(profileEntity.getId())) {
                throw new IllegalArgumentException("Unauthorized to delete this Income");
            }

            incomeRepository.delete(incomeEntity);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete Income", e);
        }
    }

    // Get Latest 5 Incomes for current user
    public Map<String, Object> getLatestFiveIncomesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            List<IncomeDTO> incomeDTOs = incomeRepository
                    .findTop5ByProfileIdOrderByDateDesc(profileEntity.getId())
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("latestIncomes", incomeDTOs);

            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get latest incomes", e);
        }
    }

    // Get total incomes for current user
    public BigDecimal getTotalIncomesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();
            BigDecimal totalIncomes = incomeRepository.findTotalIncomeByProfileId(profileEntity.getId());

            return totalIncomes != null ? totalIncomes : BigDecimal.ZERO;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get total incomes", e);
        }
    }

    // Filter Incomes
    public Map<String, Object> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            List<IncomeDTO> incomeDTOs = incomeRepository
                    .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                            profileEntity.getId(),
                            startDate,
                            endDate,
                            keyword,
                            sort)
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("incomes", incomeDTOs);
            
            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter incomes", e);
        }
    }
}
