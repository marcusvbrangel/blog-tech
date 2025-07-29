package com.blog.api.service;

import com.blog.api.dto.CategoryDTO;
import com.blog.api.entity.Category;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDTO categoryDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");
        testCategory.setDescription("Technology related posts");
        testCategory.setPosts(new ArrayList<>());

        categoryDTO = new CategoryDTO(1L, "Technology", "Technology related posts", 0);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllCategories_ShouldReturnPageOfCategoryDTO() {
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory));
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Technology", result.getContent().get(0).name());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategoryDTO() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CategoryDTO result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(testCategory.getId(), result.id());
        assertEquals(testCategory.getName(), result.name());
        assertEquals(testCategory.getDescription(), result.description());
    }

    @Test
    void getCategoryById_WhenCategoryNotExists_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.getCategoryById(1L)
        );
    }

    @Test
    void createCategory_WhenValidData_ShouldReturnCategoryDTO() {
        when(categoryRepository.existsByName("Technology")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals(testCategory.getName(), result.name());
        assertEquals(testCategory.getDescription(), result.description());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WhenNameAlreadyExists_ShouldThrowBadRequestException() {
        when(categoryRepository.existsByName("Technology")).thenReturn(true);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> categoryService.createCategory(categoryDTO)
        );

        assertEquals("Category name already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenValidData_ShouldReturnUpdatedCategoryDTO() {
        CategoryDTO updateDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Technology")).thenReturn(false);
        
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Technology");
        updatedCategory.setDescription("Updated description");
        updatedCategory.setPosts(new ArrayList<>());
        
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryDTO result = categoryService.updateCategory(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Technology", result.name());
        assertEquals("Updated description", result.description());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.updateCategory(1L, categoryDTO)
        );

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenNewNameAlreadyExists_ShouldThrowBadRequestException() {
        CategoryDTO updateDTO = new CategoryDTO(null, "Existing Category", "Updated description", 0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> categoryService.updateCategory(1L, updateDTO)
        );

        assertEquals("Category name already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenSameName_ShouldAllowUpdate() {
        CategoryDTO updateDTO = new CategoryDTO(null, "Technology", "Updated description", 0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryDTO result = categoryService.updateCategory(1L, updateDTO);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryRepository, never()).existsByName(anyString());
    }

    @Test
    void deleteCategory_WhenCategoryExists_ShouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_WhenCategoryNotExists_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.deleteCategory(1L)
        );

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}