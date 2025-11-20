package dev.hananfarizta.moneymanager.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;
import dev.hananfarizta.moneymanager.dto.FilterDTO;
import dev.hananfarizta.moneymanager.service.ExpenseService;
import dev.hananfarizta.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping()
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> filterTransactions(
            @RequestBody FilterDTO filterDTO) {

        // Preparing and validating data
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(filterDTO.getSortOrder())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(sortDirection, sortField);

        Map<String, Object> result;

        if ("income".equals(filterDTO.getType())) {
            result = incomeService.filterIncomes(startDate, endDate, keyword, sort);
        } else if ("expense".equals(filterDTO.getType())) {
            result = expenseService.filterExpenses(startDate, endDate, keyword, sort);
        } else {
            ApiResponseDTO<Map<String, Object>> errorResponse = new ApiResponseDTO<>(
                    "error",
                    "Invalid type specified. Must be 'income' or 'expense'.",
                    null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Transactions filtered successfully",
                result);

        return ResponseEntity.ok(response);
    }

}
