package dev.CaoNguyen_1883.ecommerce.product.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.CategoryRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.Category;
import dev.CaoNguyen_1883.ecommerce.product.mapper.CategoryMapper;
import dev.CaoNguyen_1883.ecommerce.product.repository.CategoryRepository;
import dev.CaoNguyen_1883.ecommerce.product.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryDto> getAllCategories() {
        log.debug("Fetching all active categories");
        return categoryRepository.findAllActive().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Cacheable(value = "categories", key = "'root'")
    public List<CategoryDto> getRootCategories() {
        log.debug("Fetching root categories");
        List<Category> rootCategories = categoryRepository.findRootCategories();

        // Load children for each root category
        return rootCategories.stream()
                .map(root -> {
                    CategoryDto dto = categoryMapper.toDto(root);
                    List<CategoryDto> children = categoryRepository.findByParentId(root.getId())
                            .stream()
                            .map(categoryMapper::toDto)
                            .toList();
                    dto.setChildren(children);
                    return dto;
                })
                .toList();
    }

    @Override
    @Cacheable(value = "categories", key = "'parent_' + #parentId")
    public List<CategoryDto> getCategoriesByParentId(UUID parentId) {
        log.debug("Fetching categories by parent ID: {}", parentId);
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(UUID id) {
        log.debug("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Cacheable(value = "categories", key = "'slug_' + #slug")
    public CategoryDto getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Check name uniqueness
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = categoryMapper.toEntity(request);

        // Generate slug
        category.setSlug(generateSlug(request.getName()));

        // Check slug uniqueness
        if (categoryRepository.existsBySlug(category.getSlug())) {
            category.setSlug(category.getSlug() + "-" + UUID.randomUUID().toString().substring(0, 8));
        }

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully: {}", saved.getId());

        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto updateCategory(UUID id, CategoryRequest request) {
        log.info("Updating category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check name uniqueness (if changed)
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        // Update fields
        categoryMapper.updateEntityFromRequest(request, category);

        // Update slug if name changed
        if (!category.getName().equals(request.getName())) {
            category.setSlug(generateSlug(request.getName()));
        }

        // Update parent if changed
        if (request.getParentId() != null) {
            // Prevent self-reference
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated successfully: {}", id);

        return categoryMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(UUID id) {
        log.info("Soft deleting category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if category has children
        if (category.hasChildren()) {
            throw new BadRequestException("Cannot delete category with sub-categories. Delete children first.");
        }

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Category soft deleted successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto restoreCategory(UUID id) {
        log.info("Restoring category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setIsActive(true);
        Category restored = categoryRepository.save(category);

        log.info("Category restored successfully: {}", id);
        return categoryMapper.toDto(restored);
    }

    // Helper method to generate slug
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}

