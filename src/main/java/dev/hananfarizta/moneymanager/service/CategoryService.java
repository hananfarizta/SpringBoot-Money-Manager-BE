package dev.hananfarizta.moneymanager.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import dev.hananfarizta.moneymanager.dto.CategoryDTO;
import dev.hananfarizta.moneymanager.entity.CategoryEntity;
import dev.hananfarizta.moneymanager.entity.ProfileEntity;
import dev.hananfarizta.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    // Save Category
    public Map<String, Object> saveCategory(CategoryDTO categoryDTO) {

        validateSavedCategory(categoryDTO);

        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            boolean nameExists = categoryRepository
                    .existsByNameAndTypeAndProfileId(categoryDTO.getName(), categoryDTO.getType(),
                            profileEntity.getId());

            if (nameExists) {
                throw new IllegalArgumentException("Category with this name already exists");
            }

            CategoryEntity newCategory = toEntity(categoryDTO, profileEntity);
            newCategory = categoryRepository.save(newCategory);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("category", toDTO(newCategory));

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }

    private void validateSavedCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            throw new IllegalArgumentException("Category data cannot be null");
        }

        if (categoryDTO.getName() == null || categoryDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }

    // Helper Methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profileEntity) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profileEntity)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .profileId(categoryEntity.getProfile() != null ? categoryEntity.getProfile().getId() : null)
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .build();
    }

    // Get categories for current user
    public Map<String, Object> getCategoriesForCurrentUser() {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            List<CategoryDTO> categoryDTOs = categoryRepository
                    .findByProfileId(profileEntity.getId())
                    .stream()
                    .map(this::toDTO)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("categories", categoryDTOs);

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get categories", e);
        }
    }

    // Get categories by type
    public Map<String, Object> getCategoriesByType(String type) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            List<CategoryDTO> categoryDTOs = categoryRepository
                    .findByTypeAndProfileId(type, profileEntity.getId())
                    .stream()
                    .map(this::toDTO)
                    .toList();

            if (categoryDTOs.isEmpty()) {
                throw new IllegalArgumentException("No categories found for type: " + type);
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("categories", categoryDTOs);

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get categories", e);
        }
    }

    // Update category
    public Map<String, Object> updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        try {
            ProfileEntity profileEntity = profileService.getCurrentProfile();

            CategoryEntity existingCategory = categoryRepository
                    .findByIdAndProfileId(categoryId, profileEntity.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or not accessible"));

            if (categoryDTO.getName() != null && categoryDTO.getName().isBlank()) {
                throw new IllegalArgumentException("Category name cannot be empty");
            }

            boolean duplicateExists = categoryRepository
                    .existsByNameAndTypeAndIconAndProfileIdAndIdNot(
                            categoryDTO.getName() != null ? categoryDTO.getName() : existingCategory.getName(),
                            categoryDTO.getType() != null ? categoryDTO.getType() : existingCategory.getType(),
                            categoryDTO.getIcon() != null ? categoryDTO.getIcon() : existingCategory.getIcon(),
                            profileEntity.getId(),
                            categoryId);

            if (duplicateExists) {
                throw new IllegalArgumentException("Category with this name and type already exists");
            }

            if (categoryDTO.getName() != null) {
                boolean nameExists = categoryRepository.existsByNameAndProfileIdAndIdNot(
                        categoryDTO.getName(),
                        profileEntity.getId(),
                        categoryId);

                if (nameExists) {
                    throw new IllegalArgumentException("Category name already exists");
                }
            }

            boolean isSameData = (categoryDTO.getName() == null
                    || existingCategory.getName().equals(categoryDTO.getName())) &&
                    (categoryDTO.getType() == null ||
                            existingCategory.getType().equals(categoryDTO.getType()))
                    &&
                    (categoryDTO.getIcon() == null ||
                            existingCategory.getIcon().equals(categoryDTO.getIcon()));

            if (isSameData) {
                throw new IllegalArgumentException("No changes detected in the category");
            }

            if (categoryDTO.getName() != null) {
                existingCategory.setName(categoryDTO.getName());
            }

            if (categoryDTO.getType() != null) {
                existingCategory.setType(categoryDTO.getType());
            }

            if (categoryDTO.getIcon() != null) {
                existingCategory.setIcon(categoryDTO.getIcon());
            }

            existingCategory = categoryRepository.save(existingCategory);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("category", toDTO(existingCategory));

            return data;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

}
