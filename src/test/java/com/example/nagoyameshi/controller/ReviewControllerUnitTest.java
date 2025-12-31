package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.ReviewService;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
public class ReviewControllerUnitTest {

  @MockBean
  private ReviewService reviewService;

  @MockBean
  private RestaurantService restaurantService;

  @Autowired
  private MockMvc mockMvc;

  private UserDetailsImpl paidMemberPrincipal() {
    Role role = new Role();
    role.setName("ROLE_PAID_MEMBER");
    User user = new User();
    user.setId(1);
    user.setName("有料会員");
    user.setEmail("paid@example.com");
    user.setPassword("password");
    user.setRole(role);
    user.setEnabled(true);
    return new UserDetailsImpl(user, List.of(new SimpleGrantedAuthority("ROLE_PAID_MEMBER")));
  }

  private UserDetailsImpl freeMemberPrincipal() {
    Role role = new Role();
    role.setName("ROLE_FREE_MEMBER");
    User user = new User();
    user.setId(2);
    user.setName("無料会員");
    user.setEmail("free@example.com");
    user.setPassword("password");
    user.setRole(role);
    user.setEnabled(true);
    return new UserDetailsImpl(user, List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));
  }

  private Restaurant restaurantFixture() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(10);
    restaurant.setName("ラーメン一番");
    restaurant.setReviews(List.of());
    return restaurant;
  }

  private Review reviewFixture(Restaurant restaurant, User user) {
    Review review = new Review();
    review.setId(100);
    review.setRestaurant(restaurant);
    review.setUser(user);
    review.setScore(5);
    review.setContent("おいしい");
    return review;
  }

  @Test
  @Description("GET /restaurants/{id}/reviews: 店舗が無い場合はリダイレクトすること")
  public void index_test_1() throws Exception {
    when(restaurantService.findRestaurantById(99)).thenReturn(Optional.empty());

    mockMvc.perform(get("/restaurants/99/reviews").with(user(paidMemberPrincipal())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/restaurants"))
        .andExpect(flash().attributeExists("errorMessage"));
  }

  @Test
  @Description("GET /restaurants/{id}/reviews: 有料会員は指定ページサイズで取得すること")
  public void index_test_2() throws Exception {
    Restaurant restaurant = restaurantFixture();
    UserDetailsImpl principal = paidMemberPrincipal();
    Pageable pageable = PageRequest.of(0, 5, Sort.by(Direction.ASC, "id"));
    Review review = reviewFixture(restaurant, principal.getUser());
    Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));
    when(reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, pageable)).thenReturn(page);
    when(reviewService.hasUserAlreadyReviewed(restaurant, principal.getUser())).thenReturn(false);

    mockMvc.perform(get("/restaurants/10/reviews").with(user(principal)))
        .andExpect(status().isOk())
        .andExpect(view().name("reviews/index"))
        .andExpect(model().attributeExists("restaurant", "reviewPage", "hasUserAlreadyReviewed"));

    verify(reviewService).findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
  }

  @Test
  @Description("GET /restaurants/{id}/reviews: 無料会員は3件固定で取得すること")
  public void index_test_3() throws Exception {
    Restaurant restaurant = restaurantFixture();
    UserDetailsImpl principal = freeMemberPrincipal();
    Review review = reviewFixture(restaurant, principal.getUser());
    Page<Review> page = new PageImpl<>(List.of(review), PageRequest.of(0, 3), 1);
    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));
    when(reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, PageRequest.of(0, 3))).thenReturn(page);
    when(reviewService.hasUserAlreadyReviewed(restaurant, principal.getUser())).thenReturn(true);

    mockMvc.perform(get("/restaurants/10/reviews").with(user(principal)))
        .andExpect(status().isOk())
        .andExpect(view().name("reviews/index"))
        .andExpect(model().attribute("hasUserAlreadyReviewed", true));

    verify(reviewService).findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, PageRequest.of(0, 3));
  }

  @Test
  @Description("POST /restaurants/{id}/reviews/create: バリデーションNGで登録画面に戻ること")
  public void create_test_1() throws Exception {
    Restaurant restaurant = restaurantFixture();
    UserDetailsImpl principal = paidMemberPrincipal();
    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));

    ReviewRegisterForm invalidForm = new ReviewRegisterForm();
    invalidForm.setContent("");

    mockMvc.perform(post("/restaurants/10/reviews/create")
        .with(user(principal))
        .with(csrf())
        .param("score", "")
        .param("content", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("reviews/register"))
        .andExpect(model().attributeExists("restaurant", "reviewRegisterForm"));

    verify(reviewService, never()).createReview(invalidForm, restaurant, principal.getUser());
  }

  @Test
  @Description("POST /restaurants/{rid}/reviews/{id}/update: 所有者でない場合はリダイレクトすること")
  public void update_test_1() throws Exception {
    Restaurant restaurant = restaurantFixture();
    User owner = new User();
    owner.setId(99);
    Role role = new Role();
    role.setName("ROLE_PAID_MEMBER");
    User principalUser = new User();
    principalUser.setId(1);
    principalUser.setRole(role);
    UserDetailsImpl principal = new UserDetailsImpl(principalUser,
        List.of(new SimpleGrantedAuthority("ROLE_PAID_MEMBER")));

    Review review = new Review();
    review.setId(5);
    review.setRestaurant(restaurant);
    review.setUser(owner);

    ReviewEditForm editForm = new ReviewEditForm(4, "まあまあ");

    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));
    when(reviewService.findReviewById(5)).thenReturn(Optional.of(review));

    mockMvc.perform(post("/restaurants/10/reviews/5/update")
        .with(user(principal))
        .with(csrf())
        .param("score", "4")
        .param("content", "まあまあ"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/restaurants/10"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(reviewService, never()).updateReview(editForm, review);
  }

  @Test
  @Description("POST /restaurants/{rid}/reviews/{id}/delete: 正常に削除できること")
  public void delete_test_1() throws Exception {
    Restaurant restaurant = restaurantFixture();
    User owner = new User();
    owner.setId(1);
    Role role = new Role();
    role.setName("ROLE_PAID_MEMBER");
    UserDetailsImpl principal = new UserDetailsImpl(owner, List.of(new SimpleGrantedAuthority("ROLE_PAID_MEMBER")));
    owner.setRole(role);

    Review review = new Review();
    review.setId(7);
    review.setRestaurant(restaurant);
    review.setUser(owner);

    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));
    when(reviewService.findReviewById(7)).thenReturn(Optional.of(review));

    mockMvc.perform(post("/restaurants/10/reviews/7/delete").with(user(principal)).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/restaurants/10"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(reviewService).deleteReview(review);
  }
}
