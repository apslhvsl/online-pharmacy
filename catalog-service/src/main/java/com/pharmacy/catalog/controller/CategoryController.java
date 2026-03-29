package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.CategoryDto;
import com.pharmacy.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public-facing category endpoints — accessible by anyone through the gateway.
 * Write operations live in InternalCategoryController (Feign-only, not gateway-routed).
 */
@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List all categories", description = "Returns all active medicine categories available in the catalog")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Get category by ID", description = "Returns the details of a single active category by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}
