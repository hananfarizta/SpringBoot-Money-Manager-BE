package dev.hananfarizta.moneymanager.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;
import dev.hananfarizta.moneymanager.dto.CategoryDTO;
import dev.hananfarizta.moneymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/category")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> saveCategory(@RequestBody CategoryDTO categoryDTO) {
        Map<String, Object> savedCategory = categoryService.saveCategory(categoryDTO);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Category created successfully",
                savedCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getCategoriesForCurrentUser() {
        Map<String, Object> data = categoryService.getCategoriesForCurrentUser();

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Categories retrieved successfully",
                data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{type}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getCategoriesByType(@PathVariable String type) {
        Map<String, Object> data = categoryService.getCategoriesByType(type);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Categories retrieved successfully",
                data);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> updateCategory(@PathVariable Long categoryId,
            @RequestBody CategoryDTO categoryDTO) {
        Map<String, Object> updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);

        ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>(
                "success",
                "Category updated successfully",
                updatedCategory);
        return ResponseEntity.ok(response);
    }
}
