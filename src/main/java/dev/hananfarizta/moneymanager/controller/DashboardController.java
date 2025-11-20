package dev.hananfarizta.moneymanager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;
import dev.hananfarizta.moneymanager.service.DashboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getDashboardData() {
        Map<String, Object> data = dashboardService.getDashboardData();

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Dashboard data retrieved successfully",
                data);

        return ResponseEntity.ok(response);
    }
}
