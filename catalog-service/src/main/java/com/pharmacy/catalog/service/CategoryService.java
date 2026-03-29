package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.CategoryCreateRequest;
import com.pharmacy.catalog.dto.CategoryDto;
import com.pharmacy.catalog.entity.Category;
import com.pharmacy.catalog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .map(this::toDto)
                .toList();
    }

    public CategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    @Transactional
    public CategoryDto createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .active(true)
                .build();
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        if (request.getName() != null) category.setName(request.getName());
        if (request.getSlug() != null) category.setSlug(request.getSlug());
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        category.setActive(false);
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        boolean hasMedicines = category.getMedicines() != null && !category.getMedicines().isEmpty();
        if (hasMedicines) throw new IllegalStateException("Cannot delete category with existing medicines");
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryDto toDto(Category c) {
        int count = (c.getMedicines() != null) ? c.getMedicines().size() : 0;
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .active(c.getActive())
                .medicineCount(count)
                .build();
    }
}
