package com.chronotrack.service;

import com.chronotrack.dto.CategoryRequest;
import com.chronotrack.dto.CategoryResponse;
import com.chronotrack.entity.Category;
import com.chronotrack.entity.User;
import com.chronotrack.exception.DuplicateResourceException;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.CategoryRepository;
import com.chronotrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing user categories and initial default category seeding.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 12 Predefined categories as per ChronoTrack specifications
    private static final List<String[]> DEFAULT_CATEGORIES = List.of(
            new String[]{"Work", "#3B82F6"},          // Blue
            new String[]{"Education", "#6366F1"},     // Indigo
            new String[]{"Learning", "#8B5CF6"},      // Purple
            new String[]{"Exercise", "#10B981"},      // Emerald
            new String[]{"Health", "#06B6D4"},        // Cyan
            new String[]{"Sleep", "#64748B"},         // Slate
            new String[]{"Personal", "#EC4899"},      // Pink
            new String[]{"Entertainment", "#F59E0B"}, // Amber
            new String[]{"Travel", "#14B8A6"},        // Teal
            new String[]{"Household", "#84CC16"},     // Lime
            new String[]{"Social", "#F97316"},        // Orange
            new String[]{"Other", "#94A3B8"}          // Cool Gray
    );

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Seeds initial 12 categories for a newly registered user.
     */
    @Transactional
    public void seedDefaultCategoriesForUser(User user) {
        List<Category> categories = new ArrayList<>();
        for (String[] cat : DEFAULT_CATEGORIES) {
            categories.add(new Category(user, cat[0], cat[1]));
        }
        categoryRepository.saveAll(categories);
    }

    /**
     * Get all categories belonging to the user.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getUserCategories(Long userId) {
        return categoryRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Fetch raw Category entity ensuring user-ownership.
     */
    @Transactional(readOnly = true)
    public Category getCategoryEntity(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    /**
     * Create a new custom category for the user.
     */
    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.getName().trim())) {
            throw new DuplicateResourceException("A category with name '" + request.getName() + "' already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Category category = new Category(user, request.getName().trim(), request.getColor().trim());
        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    /**
     * Update an existing category name or color.
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long userId, CategoryRequest request) {
        Category category = getCategoryEntity(categoryId, userId);

        String trimmedName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(trimmedName) &&
                categoryRepository.existsByUserIdAndNameIgnoreCase(userId, trimmedName)) {
            throw new DuplicateResourceException("A category with name '" + trimmedName + "' already exists.");
        }

        category.setName(trimmedName);
        category.setColor(request.getColor().trim());
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    /**
     * Delete a category belonging to the user.
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = getCategoryEntity(categoryId, userId);
        categoryRepository.delete(category);
    }

    public CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getCreatedAt()
        );
    }
}
