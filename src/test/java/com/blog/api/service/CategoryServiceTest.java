package com.blog.api.service;

import com.blog.api.dto.CategoryDTO;
import com.blog.api.entity.Category;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceTest {

    private final CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDTO categoryDTO;
    private Pageable pageable;

    CategoryServiceTest(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @BeforeEach
    void setUp() {
        testCategory = Category.of("Technology", "Tech related posts")
                .build();
        testCategory.setId(1L);

        categoryDTO = new CategoryDTO(1L, "Technology", "Tech related posts", 0);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Deve retornar uma página de CategoryDTOs quando buscar todas as categorias")
    void getAllCategories_ShouldReturnPageOfCategoryDTOs() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory), pageable, 1);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryRepository.count()).thenReturn(1L);

        // Act
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Technology");
        assertThat(result.getContent().get(0).description()).isEqualTo("Tech related posts");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(categoryRepository).findAll(pageable);
        verify(categoryRepository).count();
    }

    @Test
    @DisplayName("Deve retornar uma página vazia quando não existirem categorias")
    void getAllCategories_ShouldReturnEmptyPageWhenNoCategories() {
        // Arrange
        Page<Category> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(categoryRepository.findAll(pageable)).thenReturn(emptyPage);
        when(categoryRepository.count()).thenReturn(0L);

        // Act
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        verify(categoryRepository).findAll(pageable);
        verify(categoryRepository).count();
    }

    @Test
    @DisplayName("Deve retornar uma lista de CategoryDTOs quando buscar todas as categorias como lista")
    void getAllCategoriesList_ShouldReturnListOfCategoryDTOs() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory), pageable, 1);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // Act
        List<CategoryDTO> result = categoryService.getAllCategoriesList(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Technology");
        assertThat(result.get(0).description()).isEqualTo("Tech related posts");
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar CategoryDTO quando buscar categoria por ID existente")
    void getCategoryById_ShouldReturnCategoryDTO_WhenCategoryExists() {
        // Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // Act
        CategoryDTO result = categoryService.getCategoryById(categoryId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(categoryId);
        assertThat(result.name()).isEqualTo("Technology");
        assertThat(result.description()).isEqualTo("Tech related posts");
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar categoria por ID inexistente")
    void getCategoryById_ShouldThrowResourceNotFoundException_WhenCategoryNotExists() {
        // Arrange
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Deve criar e retornar CategoryDTO quando os dados são válidos")
    void createCategory_ShouldCreateAndReturnCategoryDTO_WhenValidData() {
        // Arrange
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);
        when(categoryRepository.existsByName("Science")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryDTO result = categoryService.createCategory(newCategoryDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Technology");
        verify(categoryRepository).existsByName("Science");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando o nome da categoria já existe")
    void createCategory_ShouldThrowBadRequestException_WhenCategoryNameAlreadyExists() {
        // Arrange
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Technology", "Tech related posts", 0);
        when(categoryRepository.existsByName("Technology")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(newCategoryDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category name already exists");
        
        verify(categoryRepository).existsByName("Technology");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar e retornar CategoryDTO quando os dados são válidos")
    void updateCategory_ShouldUpdateAndReturnCategoryDTO_WhenValidData() {
        // Arrange
        Long categoryId = 1L;
        CategoryDTO updateDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Technology")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryDTO result = categoryService.updateCategory(categoryId, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByName("Updated Technology");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria mantendo o mesmo nome quando o nome não é alterado")
    void updateCategory_ShouldUpdateWithSameName_WhenNameNotChanged() {
        // Arrange
        Long categoryId = 1L;
        CategoryDTO updateDTO = new CategoryDTO(null, "Technology", "Updated description", 0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryDTO result = categoryService.updateCategory(categoryId, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).existsByName(any());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tentar atualizar categoria inexistente")
    void updateCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotExists() {
        // Arrange
        Long categoryId = 999L;
        CategoryDTO updateDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando o novo nome já existe em outra categoria")
    void updateCategory_ShouldThrowBadRequestException_WhenNewNameAlreadyExists() {
        // Arrange
        Long categoryId = 1L;
        CategoryDTO updateDTO = new CategoryDTO(null, "Existing Category", "Updated description", 0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category name already exists");
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByName("Existing Category");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar categoria quando ela existe")
    void deleteCategory_ShouldDeleteCategory_WhenCategoryExists() {
        // Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando categoria a ser deletada não existe")
    void deleteCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotExists() {
        // Arrange
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}