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
import dev.hananfarizta.moneymanager.dto.ExpenseDTO;
import dev.hananfarizta.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expense")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addExpense(@RequestBody ExpenseDTO expenseDTO) {
        Map<String, Object> savedExpense = expenseService.addExpense(expenseDTO);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Expense added successfully",
                savedExpense);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/expenses")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getCurrentMonthExpensesForCurrentUser() {
        Map<String, Object> data = expenseService.getCurrentMonthExpensesForCurrentUser();

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Expenses retrieved successfully",
                data);

        return ResponseEntity.ok(response);
    }

    

    @DeleteMapping("/expense/{expenseId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);

        ApiResponseDTO<Void> response = new ApiResponseDTO<>(
                "success",
                "Expense deleted successfully",
                null);

        return ResponseEntity.ok(response);
    }
}
