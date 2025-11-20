package dev.hananfarizta.moneymanager.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.hananfarizta.moneymanager.dto.ExpenseDTO;
import dev.hananfarizta.moneymanager.entity.CategoryEntity;
import dev.hananfarizta.moneymanager.entity.ExpenseEntity;
import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import dev.hananfarizta.moneymanager.repository.CategoryRepository;
import dev.hananfarizta.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final ExpenseRepository expenseRepository;

    // Add Expense
    public Map<String, Object> addExpense(ExpenseDTO expenseDTO) {
        validateAddedExpense(expenseDTO);

        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            CategoryEntity categoryEntity = categoryRepository.findById(expenseDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            ExpenseEntity newExpense = toEntity(expenseDTO, profileEntity, categoryEntity);
            newExpense = expenseRepository.save(newExpense);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("expense", toDTO(newExpense));

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add expense", e);
        }
    }

    public void validateAddedExpense(ExpenseDTO expenseDTO) {
        if (expenseDTO == null) {
            throw new IllegalArgumentException("Expense data cannot be null");
        }

        if (expenseDTO.getName() == null || expenseDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Expense name cannot be empty");
        }

        if (expenseDTO.getAmount() == null || expenseDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        if (expenseDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Expense must have a valid category");
        }
    }

    // helper methods
    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profileEntity)
                .category(categoryEntity)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A")
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }

    // Retrieve Expenses for The Current month/based on the start date and end date
    public Map<String, Object> getCurrentMonthExpensesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            LocalDate now = LocalDate.now();
            LocalDate startDate = now.withDayOfMonth(1);
            LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

            List<ExpenseDTO> expenseDTOs = expenseRepository
                    .findByProfileIdAndDateBetween(profileEntity.getId(),
                            startDate, endDate)
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("expenses", expenseDTOs);

            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get expenses", e);
        }
    }

    // Delete Expense by Id for Current User
    public void deleteExpense(Long expenseId) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            ExpenseEntity expenseEntity = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

            if (!expenseEntity.getProfile().getId().equals(profileEntity.getId())) {
                throw new IllegalArgumentException("Unauthorized to delete this expense");
            }

            expenseRepository.delete(expenseEntity);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete expense", e);
        }
    }

    // Get Latest 5 Expenses for current user
    public Map<String, Object> getLatestFiveExpensesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();
            List<ExpenseDTO> expenseDTOs = expenseRepository
                    .findTop5ByProfileIdOrderByDateDesc(profileEntity.getId())
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("latestExpenses", expenseDTOs);

            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get latest expenses", e);
        }
    }

    // Get total expenses for current user
    public BigDecimal getTotalExpensesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();
            BigDecimal totalExpenses = expenseRepository.findTotalExpenseByProfileId(profileEntity.getId());

            return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get total expenses", e);
        }
    }

    // Filter Expenses
    public Map<String, Object> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            List<ExpenseDTO> expenseDTOs = expenseRepository
                    .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profileEntity.getId(), startDate, endDate,
                            keyword, sort)
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("expenses", expenseDTOs);

            return data;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter expenses", e);
        }
    }

    // Notification
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }
}
