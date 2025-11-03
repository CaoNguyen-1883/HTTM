package dev.CaoNguyen_1883.ecommerce.product.service.impl;

import dev.CaoNguyen_1883.ecommerce.common.exception.DuplicateResourceException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandDto;
import dev.CaoNguyen_1883.ecommerce.product.dto.BrandRequest;
import dev.CaoNguyen_1883.ecommerce.product.entity.Brand;
import dev.CaoNguyen_1883.ecommerce.product.mapper.BrandMapper;
import dev.CaoNguyen_1883.ecommerce.product.repository.BrandRepository;
import dev.CaoNguyen_1883.ecommerce.product.service.IBrandService;
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
public class BrandServiceImpl implements IBrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Cacheable(value = "brands", key = "'all'")
    public List<BrandDto> getAllBrands() {
        log.debug("Fetching all active brands");
        return brandRepository.findAllActive().stream()
                .map(brandMapper::toDto)
                .toList();
    }

    @Override
    @Cacheable(value = "brands", key = "'featured'")
    public List<BrandDto> getFeaturedBrands() {
        log.debug("Fetching featured brands");
        return brandRepository.findFeaturedBrands().stream()
                .map(brandMapper::toDto)
                .toList();
    }

    @Override
    @Cacheable(value = "brands", key = "#id")
    public BrandDto getBrandById(UUID id) {
        log.debug("Fetching brand by ID: {}", id);
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return brandMapper.toDto(brand);
    }

    @Override
    @Cacheable(value = "brands", key = "'slug_' + #slug")
    public BrandDto getBrandBySlug(String slug) {
        log.debug("Fetching brand by slug: {}", slug);
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "slug", slug));
        return brandMapper.toDto(brand);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto createBrand(BrandRequest request) {
        log.info("Creating new brand: {}", request.getName());

        // Check name uniqueness
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }

        Brand brand = brandMapper.toEntity(request);

        // Generate slug
        brand.setSlug(generateSlug(request.getName()));

        // Check slug uniqueness
        if (brandRepository.existsBySlug(brand.getSlug())) {
            brand.setSlug(brand.getSlug() + "-" + UUID.randomUUID().toString().substring(0, 8));
        }

        Brand saved = brandRepository.save(brand);
        log.info("Brand created successfully: {}", saved.getId());

        return brandMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto updateBrand(UUID id, BrandRequest request) {
        log.info("Updating brand ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Check name uniqueness (if changed)
        if (!brand.getName().equals(request.getName())
                && brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }

        // Update fields
        brandMapper.updateEntityFromRequest(request, brand);

        // Update slug if name changed
        if (!brand.getName().equals(request.getName())) {
            brand.setSlug(generateSlug(request.getName()));
        }

        Brand updated = brandRepository.save(brand);
        log.info("Brand updated successfully: {}", id);

        return brandMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(UUID id) {
        log.info("Soft deleting brand ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        brand.setIsActive(false);
        brandRepository.save(brand);

        log.info("Brand soft deleted successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto restoreBrand(UUID id) {
        log.info("Restoring brand ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        brand.setIsActive(true);
        Brand restored = brandRepository.save(brand);

        log.info("Brand restored successfully: {}", id);
        return brandMapper.toDto(restored);
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
