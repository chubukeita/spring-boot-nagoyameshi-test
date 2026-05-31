package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;

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

import com.example.nagoyameshi.entity.Restaurant;

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
// @ActiveProfiles("test")
public class RestaurantRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private RestaurantRepository restaurantRepository;

	private Restaurant ramen;
	private Restaurant sushi;

	@BeforeEach
	public void setUp() {
		ramen = buildRestaurant("ラーメン一番", 800, 1500);
		ramen = entityManager.persist(ramen);

		sushi = buildRestaurant("寿司太郎", 2000, 5000);
		sushi = entityManager.persist(sushi);

		entityManager.flush();
		entityManager.clear();
	}

	private Restaurant buildRestaurant(String name, int lowest, int highest) {
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setDescription("テスト用店舗");
		r.setLowestPrice(lowest);
		r.setHighestPrice(highest);
		r.setPostalCode("4600000");
		r.setAddress("名古屋市中区");
		r.setOpeningTime(LocalTime.of(10, 0));
		r.setClosingTime(LocalTime.of(22, 0));
		r.setSeatingCapacity(30);
		return r;
	}

	@Test
	@Description("findByNameLike_名前部分一致で取得できること")
	public void findByNameLike_test_1() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<Restaurant> page = restaurantRepository.findByNameLike("%寿司%", pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("寿司太郎", page.getContent().get(0).getName());
	}

	@Test
	@Description("findFirstByOrderByIdDesc_ID最大の店舗が取得できること")
	public void findFirstByOrderByIdDesc_test_1() {
		Restaurant found = restaurantRepository.findFirstByOrderByIdDesc();

		assertNotNull(found);
		assertEquals(sushi.getId(), found.getId());
		assertEquals("寿司太郎", found.getName());
	}

	@Test
	@Description("findByLowestPriceLessThanEqualOrderByLowestPriceAsc_指定価格以下で昇順取得できること")
	public void findByLowestPriceLessThanEqualOrderByLowestPriceAsc_test_1() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<Restaurant> page = restaurantRepository
				.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(2000, pageable);

		assertEquals(2, page.getTotalElements());
		assertEquals("ラーメン一番", page.getContent().get(0).getName());
		assertEquals("寿司太郎", page.getContent().get(1).getName());
	}
}
