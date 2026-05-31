package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.example.nagoyameshi.entity.Reservation;
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
// @ActiveProfiles("test")
public class ReservationRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ReservationRepository reservationRepository;

	private User user;
	private Restaurant restaurant;
	private Reservation r1;
	private Reservation r2;

	@BeforeEach
	void setUp() {
		Role role = new Role();
		role.setName("ROLE_FREE_MEMBER");
		role = entityManager.persist(role);

		user = new User();
		user.setName("テストユーザー");
		user.setFurigana("テスト ユーザー");
		user.setPostalCode("4600000");
		user.setAddress("名古屋市中区");
		user.setPhoneNumber("09000000000");
		user.setEmail("test@example.com");
		user.setPassword("dummy");
		user.setRole(role);
		user.setEnabled(true);
		user = entityManager.persist(user);

		restaurant = new Restaurant();
		restaurant.setName("テストレストラン");
		restaurant.setDescription("テスト用レストラン");
		restaurant.setAddress("名古屋市");
		restaurant.setPostalCode("4600000");
		restaurant.setLowestPrice(1000);
		restaurant.setHighestPrice(3000);
		restaurant.setOpeningTime(LocalTime.of(10, 0));
		restaurant.setClosingTime(LocalTime.of(21, 0));
		restaurant.setSeatingCapacity(20);
		restaurant = entityManager.persist(restaurant);

		r1 = new Reservation();
		r1.setUser(user);
		r1.setRestaurant(restaurant);
		r1.setReservedDatetime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)));
		r1.setNumberOfPeople(2);
		r1 = entityManager.persist(r1);

		r2 = new Reservation();
		r2.setUser(user);
		r2.setRestaurant(restaurant);
		r2.setReservedDatetime(LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(18, 0)));
		r2.setNumberOfPeople(4);
		r2 = entityManager.persist(r2);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findByUserOrderByReservedDatetimeDesc_予約日時の降順で取得できること")
	void findByUserOrderByReservedDatetimeDesc_test_1() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<Reservation> page = reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable);

		assertEquals(2, page.getTotalElements());
		assertEquals(r2.getId(), page.getContent().get(0).getId());
		assertEquals(r1.getId(), page.getContent().get(1).getId());
	}

	@Test
	@Description("findFirstByOrderByIdDesc_IDが最大の予約を取得できること")
	void findFirstByOrderByIdDesc_test_1() {
		Reservation found = reservationRepository.findFirstByOrderByIdDesc();

		assertNotNull(found);
		assertEquals(r2.getId(), found.getId());
		assertEquals(4, found.getNumberOfPeople());
	}
}
