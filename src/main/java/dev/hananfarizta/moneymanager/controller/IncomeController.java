package dev.hananfarizta.moneymanager.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;
import dev.hananfarizta.moneymanager.dto.IncomeDTO;
import dev.hananfarizta.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping("/income")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addIncome(@RequestBody IncomeDTO incomeDTO) {
        Map<String, Object> savedIncome = incomeService.addIncome(incomeDTO);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Income added successfully",
                savedIncome);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incomes")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getCurrentMonthIncomesForCurrentUser() {
        Map<String, Object> data = incomeService.getCurrentMonthIncomesForCurrentUser();

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Incomes retrieved successfully",
                data);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/income/{incomeId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteIncome(@PathVariable Long incomeId) {
        incomeService.deleteIncome(incomeId);

        ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                "success",
                "Income deleted successfully",
                null);

        return ResponseEntity.ok(response);
    }
}
