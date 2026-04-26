package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ActiveProfiles;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

import java.time.LocalTime;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = {
//		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1",
//		"spring.datasource.driverClassName=org.h2.Driver",
//		"spring.datasource.username=sa",
//		"spring.datasource.password=",
//		"spring.sql.init.mode=never",
//		"spring.jpa.hibernate.ddl-auto=create-drop"
//})
@ActiveProfiles("repository-test")
public class CategoryRestaurantRepositoryTest {
	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CategoryRestaurantRepository categoryRestaurantRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	private Category category;
	private Restaurant restaurant;
	private CategoryRestaurant categoryRestaurant;

	@BeforeEach
	void setUp() {
		// テスト用のカテゴリを作成
		category = new Category();
		category.setName("テストカテゴリ");
		entityManager.persist(category);

		// テスト用のレストランを作成
		restaurant = new Restaurant();
		restaurant.setName("テストレストラン");
		restaurant.setDescription("テスト用レストラン");
		restaurant.setLowestPrice(1000);
		restaurant.setHighestPrice(3000);
		restaurant.setPostalCode("4600000");
		restaurant.setAddress("名古屋市中区");
		restaurant.setOpeningTime(LocalTime.of(10, 0));
		restaurant.setClosingTime(LocalTime.of(22, 0));
		restaurant.setSeatingCapacity(20);
		entityManager.persist(restaurant);

		// テスト用のカテゴリレストランを作成
		categoryRestaurant = new CategoryRestaurant();
		categoryRestaurant.setCategory(category);
		categoryRestaurant.setRestaurant(restaurant);
		entityManager.persist(categoryRestaurant);

		entityManager.flush();
	}

	@Test
	@Description("findCategoryIdsByRestaurantOrderByIdAsc でレストランに関連するカテゴリIDが取得できる")
	void findCategoryIdsByRestaurantOrderByIdAsc_test_1() {
		// 実行：レストランに関連するカテゴリIDを取得
		List<Integer> categoryIds = categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);

		// 検証：期待されたカテゴリIDが含まれていることを確認
		assertNotNull(categoryIds);
		assertTrue(categoryIds.contains(category.getId()));
	}

	@Test
	@Description("findByCategoryAndRestaurant でカテゴリとレストランから関連情報が取得できる")
	void findByCategoryAndRestaurant_test_1() {
		// 実行：カテゴリとレストランを指定して検索
		Optional<CategoryRestaurant> result = categoryRestaurantRepository.findByCategoryAndRestaurant(category,
				restaurant);

		// 検証：結果が存在し、期待されたデータであることを確認
		assertTrue(result.isPresent());
		assertEquals(category.getId(), result.get().getCategory().getId());
		assertEquals(restaurant.getId(), result.get().getRestaurant().getId());
	}

	@Test
	@Description("findByRestaurantOrderByIdAsc でレストランに関連するすべてのカテゴリレストランが取得できる")
	void findByRestaurantOrderByIdAsc_test_1() {
		// 実行：レストランに関連するすべてのカテゴリレストランを取得
		List<CategoryRestaurant> results = categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant);

		// 検証：期待されたカテゴリレストランが含まれていることを確認
		assertNotNull(results);
		assertTrue(results.size() > 0);
		assertTrue(results.stream().anyMatch(cr -> cr.getCategory().getId().equals(category.getId())));
	}
}
