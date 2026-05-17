package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
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

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
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
@ActiveProfiles("test")
public class RegularHolidayRestaurantRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;

	private Restaurant ramen;
	private Restaurant sushi;
	private RegularHoliday monday;
	private RegularHoliday tuesday;
	private RegularHolidayRestaurant ramenMon;
	private RegularHolidayRestaurant ramenTue;

	@BeforeEach
	void setUp() {
		ramen = buildRestaurant("ラーメン一番");
		ramen = entityManager.persist(ramen);
		sushi = buildRestaurant("寿司太郎");
		sushi = entityManager.persist(sushi);

		monday = buildHoliday("月", 1);
		monday = entityManager.persist(monday);
		tuesday = buildHoliday("火", 2);
		tuesday = entityManager.persist(tuesday);

		ramenMon = new RegularHolidayRestaurant();
		ramenMon.setRestaurant(ramen);
		ramenMon.setRegularHoliday(monday);
		ramenMon = entityManager.persist(ramenMon);

		ramenTue = new RegularHolidayRestaurant();
		ramenTue.setRestaurant(ramen);
		ramenTue.setRegularHoliday(tuesday);
		ramenTue = entityManager.persist(ramenTue);

		RegularHolidayRestaurant sushiMon = new RegularHolidayRestaurant();
		sushiMon.setRestaurant(sushi);
		sushiMon.setRegularHoliday(monday);
		entityManager.persist(sushiMon);

		entityManager.flush();
		entityManager.clear();
	}

	private Restaurant buildRestaurant(String name) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name);
		restaurant.setDescription("テスト用店舗");
		restaurant.setLowestPrice(800);
		restaurant.setHighestPrice(1500);
		restaurant.setPostalCode("4600000");
		restaurant.setAddress("名古屋市中区");
		restaurant.setOpeningTime(LocalTime.of(10, 0));
		restaurant.setClosingTime(LocalTime.of(22, 0));
		restaurant.setSeatingCapacity(30);
		return restaurant;
	}

	private RegularHoliday buildHoliday(String day, int index) {
		RegularHoliday rh = new RegularHoliday();
		rh.setDay(day);
		rh.setDayIndex(index);
		return rh;
	}

	@Test
	@Description("findRegularHolidayIdsByRestaurant_店舗の定休日ID一覧を取得できること")
	void findRegularHolidayIdsByRestaurant_test_1() {
		List<Integer> ids = regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(ramen);

		assertEquals(2, ids.size());
		assertTrue(ids.contains(monday.getId()));
		assertTrue(ids.contains(tuesday.getId()));
	}

	@Test
	@Description("findByRegularHolidayAndRestaurant_組み合わせで取得できること")
	void findByRegularHolidayAndRestaurant_test_1() {
		Optional<RegularHolidayRestaurant> found = regularHolidayRestaurantRepository
				.findByRegularHolidayAndRestaurant(monday, ramen);

		assertTrue(found.isPresent());
		assertEquals(ramenMon.getId(), found.get().getId());
	}

	@Test
	@Description("findByRestaurant_店舗に紐づく全件を返すこと")
	void findByRestaurant_test_1() {
		List<RegularHolidayRestaurant> list = regularHolidayRestaurantRepository.findByRestaurant(ramen);

		assertEquals(2, list.size());
	}
}
