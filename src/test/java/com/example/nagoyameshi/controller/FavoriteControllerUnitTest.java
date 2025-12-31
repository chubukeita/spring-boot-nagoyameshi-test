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

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantService;

@WebMvcTest(FavoriteController.class)
public class FavoriteControllerUnitTest {

  @MockBean
  private RestaurantService restaurantService;

  @MockBean
  private FavoriteService favoriteService;

  @Autowired
  private MockMvc mockMvc;

  private UserDetailsImpl paidMemberPrincipal() {
    Role role = new Role();
    role.setName("ROLE_PAID_MEMBER");
    User user = new User();
    user.setId(1);
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
    return restaurant;
  }

  private Favorite favoriteFixture(User user, Restaurant restaurant) {
    Favorite favorite = new Favorite();
    favorite.setId(100);
    favorite.setUser(user);
    favorite.setRestaurant(restaurant);
    return favorite;
  }

  @Test
  @Description("GET /favorites: 無料会員はサブスク登録にリダイレクトすること")
  public void index_test_1() throws Exception {
    mockMvc.perform(get("/favorites").with(user(freeMemberPrincipal())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/subscription/register"))
        .andExpect(flash().attributeExists("subscriptionMessage"));

  }

  @Test
  @Description("GET /favorites: 有料会員はページングされたお気に入り一覧を取得できること")
  public void index_test_2() throws Exception {
    UserDetailsImpl principal = paidMemberPrincipal();
    User user = principal.getUser();
    Restaurant restaurant = restaurantFixture();
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.ASC, "id"));
    Page<Favorite> page = new PageImpl<>(List.of(favoriteFixture(user, restaurant)), pageable, 1);
    when(favoriteService.findFavoritesByUserOrderByCreatedAtDesc(user, pageable)).thenReturn(page);

    mockMvc.perform(get("/favorites").with(user(principal)))
        .andExpect(status().isOk())
        .andExpect(view().name("favorites/index"))
        .andExpect(model().attributeExists("favoritePage"));

    verify(favoriteService).findFavoritesByUserOrderByCreatedAtDesc(user, pageable);
  }

  @Test
  @Description("POST /restaurants/{id}/favorites/create: 無料会員はサブスク登録へリダイレクトすること")
  public void create_test_1() throws Exception {
    mockMvc.perform(post("/restaurants/10/favorites/create").with(user(freeMemberPrincipal())).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/subscription/register"))
        .andExpect(flash().attributeExists("subscriptionMessage"));

  }

  @Test
  @Description("POST /restaurants/{id}/favorites/create: 店舗が存在しない場合はリダイレクトすること")
  public void create_test_2() throws Exception {
    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.empty());

    mockMvc.perform(post("/restaurants/10/favorites/create").with(user(paidMemberPrincipal())).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/restaurants"))
        .andExpect(flash().attributeExists("errorMessage"));

  }

  @Test
  @Description("POST /restaurants/{id}/favorites/create: 成功時は店舗詳細にリダイレクトすること")
  public void create_test_3() throws Exception {
    Restaurant restaurant = restaurantFixture();
    UserDetailsImpl principal = paidMemberPrincipal();
    when(restaurantService.findRestaurantById(10)).thenReturn(Optional.of(restaurant));

    mockMvc.perform(post("/restaurants/10/favorites/create").with(user(principal)).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/restaurants/10"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(favoriteService).createFavorite(restaurant, principal.getUser());
  }

  @Test
  @Description("POST /favorites/{id}/delete: 無料会員はサブスク登録へリダイレクトすること")
  void delete_free_redirectsSubscription() throws Exception {
    mockMvc.perform(post("/favorites/5/delete").with(user(freeMemberPrincipal())).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/subscription/register"))
        .andExpect(flash().attributeExists("subscriptionMessage"));

  }

  @Test
  @Description("POST /favorites/{id}/delete: お気に入りが存在しない場合はリファラーにリダイレクトすること")
  void delete_notFound_redirectsReferer() throws Exception {
    Favorite expectedFavorite = new Favorite();
    expectedFavorite.setId(99);
    when(favoriteService.findFavoriteById(99)).thenReturn(Optional.empty());

    mockMvc.perform(post("/favorites/99/delete")
        .with(user(paidMemberPrincipal()))
        .with(csrf())
        .header("Referer", "/favorites"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/favorites"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(favoriteService).findFavoriteById(99);
    verify(favoriteService, never()).deleteFavorite(expectedFavorite);
  }

  @Test
  @Description("POST /favorites/{id}/delete: 所有者でない場合はリファラーにリダイレクトすること")
  void delete_notOwner_redirectsReferer() throws Exception {
    User owner = new User();
    owner.setId(99);
    Favorite favorite = new Favorite();
    favorite.setId(5);
    favorite.setUser(owner);
    when(favoriteService.findFavoriteById(5)).thenReturn(Optional.of(favorite));

    mockMvc.perform(post("/favorites/5/delete")
        .with(user(paidMemberPrincipal()))
        .with(csrf())
        .header("Referer", "/favorites"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/favorites"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(favoriteService, never()).deleteFavorite(favorite);
  }

  @Test
  @Description("POST /favorites/{id}/delete: 所有者は削除できること")
  void delete_owner_canDelete() throws Exception {
    UserDetailsImpl principal = paidMemberPrincipal();
    User owner = principal.getUser();
    Favorite favorite = new Favorite();
    favorite.setId(7);
    favorite.setUser(owner);
    when(favoriteService.findFavoriteById(7)).thenReturn(Optional.of(favorite));

    mockMvc.perform(post("/favorites/7/delete")
        .with(user(principal))
        .with(csrf())
        .header("Referer", "/favorites"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/favorites"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(favoriteService).deleteFavorite(favorite);
  }
}
