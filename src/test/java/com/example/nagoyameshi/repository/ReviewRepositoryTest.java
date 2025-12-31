package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.springframework.test.context.TestPropertySource;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:review_repository_test;MODE=MySQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ReviewRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ReviewRepository reviewRepository;

  private Restaurant restaurant;
  private User user;
  private Review firstReview;
  private Review secondReview;

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

    restaurant = new Restaurant();
    restaurant.setName("ラーメン一番");
    restaurant.setDescription("テスト用店舗");
    restaurant.setLowestPrice(800);
    restaurant.setHighestPrice(1500);
    restaurant.setPostalCode("4600000");
    restaurant.setAddress("名古屋市中区");
    restaurant.setOpeningTime(LocalTime.of(10, 0));
    restaurant.setClosingTime(LocalTime.of(22, 0));
    restaurant.setSeatingCapacity(30);
    restaurant = entityManager.persist(restaurant);

    firstReview = new Review();
    firstReview.setContent("おいしい");
    firstReview.setScore(5);
    firstReview.setRestaurant(restaurant);
    firstReview.setUser(user);
    firstReview = entityManager.persist(firstReview);

    // Ensure created_at differs
    Thread.sleep(1000L);

    User anotherUser = new User();
    anotherUser.setName("別ユーザー");
    anotherUser.setFurigana("ベツユーザー");
    anotherUser.setPostalCode("4600000");
    anotherUser.setAddress("名古屋市中区");
    anotherUser.setPhoneNumber("08000000000");
    anotherUser.setEmail("other@example.com");
    anotherUser.setPassword("password");
    anotherUser.setRole(role);
    anotherUser.setEnabled(true);
    anotherUser = entityManager.persist(anotherUser);

    secondReview = new Review();
    secondReview.setContent("ふつう");
    secondReview.setScore(3);
    secondReview.setRestaurant(restaurant);
    secondReview.setUser(anotherUser);
    secondReview = entityManager.persist(secondReview);

    entityManager.flush();

    // created_at is managed by DB defaults in production; set manually here for deterministic ordering
    entityManager.getEntityManager().createNativeQuery(
        "UPDATE reviews SET created_at = ? WHERE id = ?")
        .setParameter(1, Timestamp.valueOf("2024-01-01 10:00:00"))
        .setParameter(2, firstReview.getId())
        .executeUpdate();

    entityManager.getEntityManager().createNativeQuery(
        "UPDATE reviews SET created_at = ? WHERE id = ?")
        .setParameter(1, Timestamp.valueOf("2024-01-01 10:00:01"))
        .setParameter(2, secondReview.getId())
        .executeUpdate();

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @Description("findByRestaurantAndUser_店舗とユーザーで取得できること")
  void findByRestaurantAndUser_test_1() {
    Review found = reviewRepository.findByRestaurantAndUser(restaurant, user);

    assertNotNull(found);
    assertEquals(firstReview.getId(), found.getId());
    assertEquals(user.getId(), found.getUser().getId());
  }

  @Test
  @Description("findByRestaurantOrderByCreatedAtDesc_作成日時降順で取得できること")
  void findByRestaurantOrderByCreatedAtDesc_test_1() {
    Pageable pageable = PageRequest.of(0, 5);

    Page<Review> page = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);

    assertEquals(2, page.getTotalElements());
    List<Review> reviews = page.getContent();
    assertEquals(secondReview.getId(), reviews.get(0).getId());
    assertEquals(firstReview.getId(), reviews.get(1).getId());
  }

  @Test
  @Description("findFirstByOrderByIdDesc_IDが最大のレビューを取得できること")
  void findFirstByOrderByIdDesc_test_1() {
    Review found = reviewRepository.findFirstByOrderByIdDesc();

    assertNotNull(found);
    assertEquals(secondReview.getId(), found.getId());
  }
}
