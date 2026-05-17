package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;

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
@ActiveProfiles("test")
public class FavoriteRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FavoriteRepository favoriteRepository;

	private User user;
	private Restaurant ramen;
	private Restaurant sushi;
	private Favorite ramenFavorite;
	private Favorite sushiFavorite;

	@BeforeEach
	void setUp() throws InterruptedException {
		Role role = new Role();
		role.setName("ROLE_PAID_MEMBER");
		role = entityManager.persist(role);

		user = new User();
		user.setName("テストユーザー");
		user.setFurigana("テストユーザー");
		user.setPostalCode("4600000");
		user.setAddress("名古屋市中区");
		user.setPhoneNumber("09000000000");
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setRole(role);
		user.setEnabled(true);
		user = entityManager.persist(user);

		ramen = buildRestaurant("ラーメン一番", 800, 1500);
		ramen = entityManager.persist(ramen);

		sushi = buildRestaurant("寿司太郎", 2000, 5000);
		sushi = entityManager.persist(sushi);

		ramenFavorite = new Favorite();
		ramenFavorite.setRestaurant(ramen);
		ramenFavorite.setUser(user);
		ramenFavorite = entityManager.persist(ramenFavorite);

		Thread.sleep(1000L);

		sushiFavorite = new Favorite();
		sushiFavorite.setRestaurant(sushi);
		sushiFavorite.setUser(user);
		sushiFavorite = entityManager.persist(sushiFavorite);

		entityManager.flush();

		// insertable/updatable が false のため、手動で created_at を設定して並び順を安定させる
		entityManager.getEntityManager().createNativeQuery(
				"UPDATE favorites SET created_at = ? WHERE id = ?")
				.setParameter(1, Timestamp.valueOf("2024-01-01 10:00:00"))
				.setParameter(2, ramenFavorite.getId())
				.executeUpdate();

		entityManager.getEntityManager().createNativeQuery(
				"UPDATE favorites SET created_at = ? WHERE id = ?")
				.setParameter(1, Timestamp.valueOf("2024-01-01 10:00:01"))
				.setParameter(2, sushiFavorite.getId())
				.executeUpdate();

		entityManager.flush();
		entityManager.clear();
	}

	private Restaurant buildRestaurant(String name, int lowest, int highest) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name);
		restaurant.setDescription("テスト用店舗");
		restaurant.setLowestPrice(lowest);
		restaurant.setHighestPrice(highest);
		restaurant.setPostalCode("4600000");
		restaurant.setAddress("名古屋市中区");
		restaurant.setOpeningTime(LocalTime.of(10, 0));
		restaurant.setClosingTime(LocalTime.of(22, 0));
		restaurant.setSeatingCapacity(30);
		return restaurant;
	}

	@Test
	@Description("findByRestaurantAndUser_店舗とユーザーで取得できること")
	void findByRestaurantAndUser_test_1() {
		Favorite found = favoriteRepository.findByRestaurantAndUser(ramen, user);

		assertNotNull(found);
		assertEquals(ramenFavorite.getId(), found.getId());
		assertEquals(user.getId(), found.getUser().getId());
	}

	@Test
	@Description("findByUserOrderByCreatedAtDesc_作成日時降順で取得できること")
	void findByUserOrderByCreatedAtDesc_test_1() {
		Pageable pageable = PageRequest.of(0, 5);

		Page<Favorite> page = favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);

		assertEquals(2, page.getTotalElements());
		List<Favorite> favorites = page.getContent();
		assertEquals(sushiFavorite.getId(), favorites.get(0).getId());
		assertEquals(ramenFavorite.getId(), favorites.get(1).getId());
	}
}
