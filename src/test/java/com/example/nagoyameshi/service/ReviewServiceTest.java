package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private ReviewService reviewService;

  @Test
  @Description("findReviewById_idで委譲すること")
  void findReviewById_test_1() {
    Review review = new Review();
    review.setId(1);
    when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

    Optional<Review> result = reviewService.findReviewById(1);

    assertTrue(result.isPresent());
    verify(reviewRepository).findById(1);
  }

  @Test
  @Description("findReviewsByRestaurantOrderByCreatedAtDesc_ページングして返すこと")
  void findReviewsByRestaurantOrderByCreatedAtDesc_test_1() {
    Restaurant restaurant = new Restaurant();
    Pageable pageable = PageRequest.of(0, 5);
    Page<Review> page = new PageImpl<>(java.util.List.of(new Review()), pageable, 1);
    when(reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable)).thenReturn(page);

    Page<Review> result = reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, pageable);

    assertEquals(1, result.getTotalElements());
    verify(reviewRepository).findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
  }

  @Test
  @Description("countReviews_件数を返すこと")
  void countReviews_test_1() {
    when(reviewRepository.count()).thenReturn(3L);

    long count = reviewService.countReviews();

    assertEquals(3L, count);
    verify(reviewRepository).count();
  }

  @Test
  @Description("findFirstReviewByOrderByIdDesc_最新を返すこと")
  void findFirstReviewByOrderByIdDesc_test_1() {
    Review review = new Review();
    review.setId(5);
    when(reviewRepository.findFirstByOrderByIdDesc()).thenReturn(review);

    Review result = reviewService.findFirstReviewByOrderByIdDesc();

    assertEquals(5, result.getId());
    verify(reviewRepository).findFirstByOrderByIdDesc();
  }

  @Test
  @Description("createReview_フォームの値で保存すること")
  void createReview_test_1() {
    ReviewRegisterForm form = new ReviewRegisterForm();
    form.setContent("おいしい");
    form.setScore(5);
    Restaurant restaurant = new Restaurant();
    User user = new User();

    reviewService.createReview(form, restaurant, user);

    Review expected = new Review();
    expected.setContent("おいしい");
    expected.setScore(5);
    expected.setRestaurant(restaurant);
    expected.setUser(user);
    verify(reviewRepository).save(expected);
  }

  @Test
  @Description("updateReview_編集フォームの値で保存すること")
  void updateReview_updatesFields() {
    ReviewEditForm form = new ReviewEditForm(4, "まあまあ");
    Review review = new Review();

    reviewService.updateReview(form, review);

    assertEquals(4, review.getScore());
    assertEquals("まあまあ", review.getContent());
    verify(reviewRepository).save(review);
  }

  @Test
  @Description("deleteReview_削除を委譲すること")
  void deleteReview_deletesEntity() {
    Review review = new Review();

    reviewService.deleteReview(review);

    verify(reviewRepository).delete(review);
  }

  @Test
  @Description("hasUserAlreadyReviewed_存在すればtrue")
  void hasUserAlreadyReviewed_returnsTrueWhenFound() {
    Restaurant restaurant = new Restaurant();
    User user = new User();
    when(reviewRepository.findByRestaurantAndUser(restaurant, user)).thenReturn(new Review());

    assertTrue(reviewService.hasUserAlreadyReviewed(restaurant, user));
  }

  @Test
  @Description("hasUserAlreadyReviewed_存在しなければfalse")
  void hasUserAlreadyReviewed_returnsFalseWhenMissing() {
    Restaurant restaurant = new Restaurant();
    User user = new User();
    when(reviewRepository.findByRestaurantAndUser(restaurant, user)).thenReturn(null);

    assertFalse(reviewService.hasUserAlreadyReviewed(restaurant, user));
  }
}
