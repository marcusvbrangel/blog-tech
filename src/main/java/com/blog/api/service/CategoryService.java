package com.blog.api.service;

import com.blog.api.dto.CategoryDTO;
import com.blog.api.entity.Category;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    public CategoryRepository categoryRepository;

    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        // Get cached list data
        List<CategoryDTO> categoryDTOs = getAllCategoriesList(pageable);
        
        // Count total elements for pagination
        long totalElements = categoryRepository.count();
        
        // Return a new PageImpl with the cached DTOs
        return new org.springframework.data.domain.PageImpl<>(
            categoryDTOs, 
            pageable, 
            totalElements
        );
    }
    
    @Cacheable(value = "categories", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public List<CategoryDTO> getAllCategoriesList(Pageable pageable) {
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categories", key = "'single:' + #id")
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDTO.fromEntity(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.name())) {
            throw new BadRequestException("Category name already exists");
        }

        Category category = new Category();
        category.setName(categoryDTO.name());
        category.setDescription(categoryDTO.description());

        Category savedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(savedCategory);
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", key = "'single:' + #id"),
            @CacheEvict(value = "categories", allEntries = true)
    })
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equals(categoryDTO.name()) && 
            categoryRepository.existsByName(categoryDTO.name())) {
            throw new BadRequestException("Category name already exists");
        }

        category.setName(categoryDTO.name());
        category.setDescription(categoryDTO.description());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(updatedCategory);
    }

    @Caching(evict = {
            @CacheEvict(value = "categories", key = "'single:' + #id"),
            @CacheEvict(value = "categories", allEntries = true)
    })
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }
}