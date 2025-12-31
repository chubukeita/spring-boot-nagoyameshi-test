package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryRestaurantServiceTest {

	@InjectMocks
	private CategoryRestaurantService categoryRestaurantService;

	@Mock
	private CategoryRestaurantRepository categoryRestaurantRepository;

	@Mock
	private CategoryService categoryService;

	@Test
	@Description("findCategoryIdsByRestaurantOrderByIdAsc: 指定した店舗のカテゴリIDリストを取得できること")
	public void findCategoryIdsByRestaurantOrderByIdAsc_test_1() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		List<Integer> expectedIds = Arrays.asList(1, 2, 3);
		when(categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant))
				.thenReturn(expectedIds);

		List<Integer> result = categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);

		assertEquals(expectedIds, result);
		verify(categoryRestaurantRepository).findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}

	@Test
	@Description("createCategoriesRestaurants: カテゴリIDリストから新規のカテゴリレストラン関連を作成できること")
	public void createCategoriesRestaurants_test_1() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		Category category1 = new Category();
		category1.setId(1);
		Category category2 = new Category();
		category2.setId(2);

		List<Integer> categoryIds = Arrays.asList(1, 2);

		when(categoryService.findCategoryById(1)).thenReturn(Optional.of(category1));
		when(categoryService.findCategoryById(2)).thenReturn(Optional.of(category2));
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category1, restaurant))
				.thenReturn(Optional.empty());
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category2, restaurant))
				.thenReturn(Optional.empty());

		categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);

		CategoryRestaurant expectedCategoryRestaurant1 = new CategoryRestaurant();
		expectedCategoryRestaurant1.setRestaurant(restaurant);
		expectedCategoryRestaurant1.setCategory(category1);

		CategoryRestaurant expectedCategoryRestaurant2 = new CategoryRestaurant();
		expectedCategoryRestaurant2.setRestaurant(restaurant);
		expectedCategoryRestaurant2.setCategory(category2);

		verify(categoryService).findCategoryById(1);
		verify(categoryService).findCategoryById(2);
		verify(categoryRestaurantRepository).findByCategoryAndRestaurant(category1, restaurant);
		verify(categoryRestaurantRepository).findByCategoryAndRestaurant(category2, restaurant);
		verify(categoryRestaurantRepository).save(expectedCategoryRestaurant1);
		verify(categoryRestaurantRepository).save(expectedCategoryRestaurant2);
	}

	@Test
	@Description("createCategoriesRestaurants: 既に存在するカテゴリレストラン関連は重複して作成されないこと")
	public void createCategoriesRestaurants_test_2() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		Category category1 = new Category();
		category1.setId(1);

		CategoryRestaurant existingCategoryRestaurant = new CategoryRestaurant();
		existingCategoryRestaurant.setRestaurant(restaurant);
		existingCategoryRestaurant.setCategory(category1);

		List<Integer> categoryIds = Arrays.asList(1);

		when(categoryService.findCategoryById(1)).thenReturn(Optional.of(category1));
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category1, restaurant))
				.thenReturn(Optional.of(existingCategoryRestaurant));

		categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);

		verify(categoryService).findCategoryById(1);
		verify(categoryRestaurantRepository).findByCategoryAndRestaurant(category1, restaurant);
		verify(categoryRestaurantRepository, never()).save(existingCategoryRestaurant);
	}

	@Test
	@Description("syncCategoriesRestaurants: newCategoryIdsがnullの場合はすべての関連が削除されること")
	public void syncCategoriesRestaurants_test_1() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		CategoryRestaurant categoryRestaurant1 = new CategoryRestaurant();
		categoryRestaurant1.setId(1);
		CategoryRestaurant categoryRestaurant2 = new CategoryRestaurant();
		categoryRestaurant2.setId(2);
		List<CategoryRestaurant> existingRelations = Arrays.asList(categoryRestaurant1, categoryRestaurant2);

		when(categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant))
				.thenReturn(existingRelations);

		categoryRestaurantService.syncCategoriesRestaurants(null, restaurant);

		verify(categoryRestaurantRepository).findByRestaurantOrderByIdAsc(restaurant);
		verify(categoryRestaurantRepository).delete(categoryRestaurant1);
		verify(categoryRestaurantRepository).delete(categoryRestaurant2);
	}

	@Test
	@Description("syncCategoriesRestaurants: 新しいリストに存在しない既存の関連が削除されること")
	public void syncCategoriesRestaurants_test_2() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		Category category1 = new Category();
		category1.setId(1);
		Category category2 = new Category();
		category2.setId(2);

		CategoryRestaurant categoryRestaurant1 = new CategoryRestaurant();
		categoryRestaurant1.setCategory(category1);
		CategoryRestaurant categoryRestaurant2 = new CategoryRestaurant();
		categoryRestaurant2.setCategory(category2);

		List<CategoryRestaurant> existingRelations = Arrays.asList(categoryRestaurant1, categoryRestaurant2);
		List<Integer> newCategoryIds = Arrays.asList(1); // category2を削除

		when(categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant))
				.thenReturn(existingRelations);
		when(categoryService.findCategoryById(1)).thenReturn(Optional.of(category1));
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category1, restaurant))
				.thenReturn(Optional.of(categoryRestaurant1));

		categoryRestaurantService.syncCategoriesRestaurants(newCategoryIds, restaurant);

		verify(categoryRestaurantRepository).findByRestaurantOrderByIdAsc(restaurant);
		verify(categoryRestaurantRepository, never()).delete(categoryRestaurant1);
		verify(categoryRestaurantRepository).delete(categoryRestaurant2);
	}

	@Test
	@Description("syncCategoriesRestaurants: 新しいカテゴリIDが追加された場合は新規の関連が作成されること")
	public void syncCategoriesRestaurants_test_3() {
		Restaurant restaurant = new Restaurant();
		restaurant.setId(1);

		Category category1 = new Category();
		category1.setId(1);
		Category category2 = new Category();
		category2.setId(2);

		CategoryRestaurant categoryRestaurant1 = new CategoryRestaurant();
		categoryRestaurant1.setCategory(category1);

		List<CategoryRestaurant> existingRelations = Arrays.asList(categoryRestaurant1);
		List<Integer> newCategoryIds = Arrays.asList(1, 2); // category2を追加

		when(categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant))
				.thenReturn(existingRelations);
		when(categoryService.findCategoryById(1)).thenReturn(Optional.of(category1));
		when(categoryService.findCategoryById(2)).thenReturn(Optional.of(category2));
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category1, restaurant))
				.thenReturn(Optional.of(categoryRestaurant1));
		when(categoryRestaurantRepository.findByCategoryAndRestaurant(category2, restaurant))
				.thenReturn(Optional.empty());

		categoryRestaurantService.syncCategoriesRestaurants(newCategoryIds, restaurant);

		CategoryRestaurant expectedNewCategoryRestaurant = new CategoryRestaurant();
		expectedNewCategoryRestaurant.setRestaurant(restaurant);
		expectedNewCategoryRestaurant.setCategory(category2);

		verify(categoryRestaurantRepository).findByRestaurantOrderByIdAsc(restaurant);
		verify(categoryService).findCategoryById(1);
		verify(categoryService).findCategoryById(2);
		verify(categoryRestaurantRepository).save(expectedNewCategoryRestaurant);
	}
}

