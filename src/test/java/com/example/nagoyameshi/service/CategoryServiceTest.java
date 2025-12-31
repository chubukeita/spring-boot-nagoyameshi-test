package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private Category izakaya;
    private Category sushi;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        izakaya = new Category();
        izakaya.setId(1);
        izakaya.setName("居酒屋");

        sushi = new Category();
        sushi.setId(2);
        sushi.setName("寿司");

        pageable = PageRequest.of(0, 15);
    }

    @Test
    @Description("findAllCategories_ページングで全件を取得すること")
    void findAllCategories_test_1() {
        Page<Category> page = new PageImpl<>(List.of(izakaya, sushi));
        when(categoryRepository.findAll(pageable)).thenReturn(page);

        Page<Category> actual = categoryService.findAllCategories(pageable);

        verify(categoryRepository, times(1)).findAll(pageable);
        assertEquals(page, actual);
    }

    @Test
    @Description("findCategoriesByNameLike_名前部分一致でページング取得すること")
    void findCategoriesByNameLike_test_1() {
        Page<Category> page = new PageImpl<>(List.of(sushi));
        when(categoryRepository.findByNameLike("%寿%", pageable)).thenReturn(page);

        Page<Category> actual = categoryService.findCategoriesByNameLike("寿", pageable);

        verify(categoryRepository, times(1)).findByNameLike("%寿%", pageable);
        assertEquals(page, actual);
    }

    @Test
    @Description("findCategoryById_IDでカテゴリを取得できること")
    void findCategoryById_test_1() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(izakaya));

        Optional<Category> actual = categoryService.findCategoryById(1);

        verify(categoryRepository, times(1)).findById(1);
        assertTrue(actual.isPresent());
        assertEquals(izakaya, actual.get());
    }

    @Test
    @Description("countCategories_カテゴリ件数を返すこと")
    void countCategories_test_1() {
        when(categoryRepository.count()).thenReturn(2L);

        long count = categoryService.countCategories();

        verify(categoryRepository, times(1)).count();
        assertEquals(2L, count);
    }

    @Test
    @Description("findFirstCategoryByOrderByIdDesc_ID最大のカテゴリを返すこと")
    void findFirstCategoryByOrderByIdDesc_test_1() {
        when(categoryRepository.findFirstByOrderByIdDesc()).thenReturn(sushi);

        Category latest = categoryService.findFirstCategoryByOrderByIdDesc();

        verify(categoryRepository, times(1)).findFirstByOrderByIdDesc();
        assertEquals(sushi, latest);
    }

    @Test
    @Description("findAllCategories_リスト形式ですべて取得できること")
    void findAllCategories_test_2() {
        when(categoryRepository.findAll()).thenReturn(List.of(izakaya, sushi));

        List<Category> actual = categoryService.findAllCategories();

        verify(categoryRepository, times(1)).findAll();
        assertEquals(List.of(izakaya, sushi), actual);
    }

    @Test
    @Description("findFirstCategoryByName_名称一致で1件取得できること")
    void findFirstCategoryByName_test_1() {
        when(categoryRepository.findFirstByName("居酒屋")).thenReturn(izakaya);

        Category found = categoryService.findFirstCategoryByName("居酒屋");

        verify(categoryRepository, times(1)).findFirstByName("居酒屋");
        assertEquals(izakaya, found);
    }

    @Test
    @Description("createCategory_フォームの名称で新規登録すること")
    void createCategory_test_1() {
        CategoryRegisterForm form = new CategoryRegisterForm();
        form.setName("新カテゴリ");

        Category expected = new Category();
        expected.setName("新カテゴリ");

        categoryService.createCategory(form);

        verify(categoryRepository, times(1)).save(expected);
    }

    @Test
    @Description("updateCategory_フォームの名称で既存カテゴリを更新すること")
    void updateCategory_test_1() {
        CategoryEditForm form = new CategoryEditForm();
        form.setName("更新後");

        categoryService.updateCategory(form, izakaya);

        verify(categoryRepository, times(1)).save(izakaya);
        assertEquals("更新後", izakaya.getName());
    }

    @Test
    @Description("deleteCategory_指定カテゴリを削除すること")
    void deleteCategory_test_1() {
        categoryService.deleteCategory(izakaya);

        verify(categoryRepository, times(1)).delete(izakaya);
    }
}
