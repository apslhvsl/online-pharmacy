package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.CategoryCreateRequest;
import com.pharmacy.catalog.dto.CategoryDto;
import com.pharmacy.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal category endpoints — NOT gateway-routed.
 * Called exclusively by Admin Service via Feign.
 */
@RestController
@RequestMapping("/api/catalog/internal/categories")
@RequiredArgsConstructor
public class InternalCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", description = "Creates a new medicine category in the catalog. For internal use by Admin Service only.")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @Operation(summary = "Update a category", description = "Replaces all fields of an existing category with the provided data. For internal use by Admin Service only.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Deactivate a category", description = "Marks a category as inactive so it no longer appears in the public catalog. For internal use by Admin Service only.")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CategoryDto> deactivateCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deactivateCategory(id));
    }
}
