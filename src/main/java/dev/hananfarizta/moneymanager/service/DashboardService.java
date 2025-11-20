package dev.hananfarizta.moneymanager.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import dev.hananfarizta.moneymanager.dto.ExpenseDTO;
import dev.hananfarizta.moneymanager.dto.IncomeDTO;
import dev.hananfarizta.moneymanager.dto.RecentTransactionDTO;
import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> returnValue = new LinkedHashMap<>();

        ProfileEntity profileEntity = profileService.getCurrentProfile();

        Map<String, Object> incomeMap = incomeService.getLatestFiveIncomesForCurrentUser();
        Map<String, Object> expenseMap = expenseService.getLatestFiveExpensesForCurrentUser();

        List<IncomeDTO> latestIncomes = (List<IncomeDTO>) incomeMap.getOrDefault("latestIncomes", List.of());
        List<ExpenseDTO> latestExpenses = (List<ExpenseDTO>) expenseMap.getOrDefault("latestExpenses", List.of());

        List<RecentTransactionDTO> recentTransactionDTO = concat(
                latestIncomes.stream().map(income -> RecentTransactionDTO.builder()
                        .id(income.getId())
                        .profileId(profileEntity.getId())
                        .icon(income.getIcon())
                        .name(income.getName())
                        .amount(income.getAmount())
                        .date(income.getDate())
                        .createdAt(income.getCreatedAt())
                        .updatedAt(income.getUpdatedAt())
                        .type("income")
                        .build()),
                latestExpenses.stream().map(expense -> RecentTransactionDTO.builder()
                        .id(expense.getId())
                        .profileId(profileEntity.getId())
                        .icon(expense.getIcon())
                        .name(expense.getName())
                        .amount(expense.getAmount())
                        .date(expense.getDate())
                        .createdAt(expense.getCreatedAt())
                        .updatedAt(expense.getUpdatedAt())
                        .type("expense")
                        .build()))
                .sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                })
                .toList();

        returnValue.put("totalBalance",
                incomeService.getTotalIncomesForCurrentUser()
                        .subtract(expenseService.getTotalExpensesForCurrentUser()));

        returnValue.put("totalIncomes", incomeService.getTotalIncomesForCurrentUser());
        returnValue.put("totalExpenses", expenseService.getTotalExpensesForCurrentUser());

        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);

        returnValue.put("recentTransactions", recentTransactionDTO);

        return returnValue;
    }

    private <T> Stream<T> concat(Stream<T> a, Stream<T> b) {
        return Stream.concat(a, b);
    }
}
