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

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

  @Mock
  private FavoriteRepository favoriteRepository;

  @InjectMocks
  private FavoriteService favoriteService;

  @Test
  @Description("findFavoriteById_IDでお気に入りを取得できること")
  public void findFavoriteById_test_1() {
    Favorite favorite = new Favorite();
    favorite.setId(1);
    when(favoriteRepository.findById(1)).thenReturn(Optional.of(favorite));

    Optional<Favorite> result = favoriteService.findFavoriteById(1);

    assertTrue(result.isPresent());
    verify(favoriteRepository).findById(1);
  }

  @Test
  @Description("findFavoriteByRestaurantAndUser_店舗とユーザーでお気に入りを取得できること")
  public void findFavoriteByRestaurantAndUser_test_1() {
    Restaurant restaurant = new Restaurant();
    User user = new User();
    Favorite favorite = new Favorite();
    when(favoriteRepository.findByRestaurantAndUser(restaurant, user)).thenReturn(favorite);

    Favorite result = favoriteService.findFavoriteByRestaurantAndUser(restaurant, user);

    assertEquals(favorite, result);
    verify(favoriteRepository).findByRestaurantAndUser(restaurant, user);
  }

  @Test
  @Description("findFavoritesByUserOrderByCreatedAtDesc_ユーザーのお気に入り一覧を作成日時の降順でページング取得できること")
  public void findFavoritesByUserOrderByCreatedAtDesc_test_1() {
    User user = new User();
    Pageable pageable = PageRequest.of(0, 5);
    Page<Favorite> page = new PageImpl<>(java.util.List.of(new Favorite()), pageable, 1);
    when(favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable)).thenReturn(page);

    Page<Favorite> result = favoriteService.findFavoritesByUserOrderByCreatedAtDesc(user, pageable);

    assertEquals(1, result.getTotalElements());
    verify(favoriteRepository).findByUserOrderByCreatedAtDesc(user, pageable);
  }

  @Test
  @Description("countFavorites_お気に入り件数を返すこと")
  public void countFavorites_test_1() {
    when(favoriteRepository.count()).thenReturn(2L);

    long count = favoriteService.countFavorites();

    assertEquals(2L, count);
    verify(favoriteRepository).count();
  }

  @Test
  @Description("createFavorite_店舗とユーザーを指定してお気に入りを新規作成できること")
  public void createFavorite_test_1() {
    Restaurant restaurant = new Restaurant();
    User user = new User();

    favoriteService.createFavorite(restaurant, user);

    Favorite expected = new Favorite();
    expected.setRestaurant(restaurant);
    expected.setUser(user);
    verify(favoriteRepository).save(expected);
  }

  @Test
  @Description("deleteFavorite_指定したお気に入りを削除できること")
  public void deleteFavorite_test_1() {
    Favorite favorite = new Favorite();

    favoriteService.deleteFavorite(favorite);

    verify(favoriteRepository).delete(favorite);
  }

  @Test
  @Description("isFavorite_お気に入りが存在する場合はtrueを返すこと")
  public void isFavorite_test_1() {
    Restaurant restaurant = new Restaurant();
    User user = new User();
    when(favoriteRepository.findByRestaurantAndUser(restaurant, user)).thenReturn(new Favorite());

    assertTrue(favoriteService.isFavorite(restaurant, user));
  }

  @Test
  @Description("isFavorite_お気に入りが存在しない場合はfalseを返すこと")
  public void isFavorite_test_2() {
    Restaurant restaurant = new Restaurant();
    User user = new User();
    when(favoriteRepository.findByRestaurantAndUser(restaurant, user)).thenReturn(null);

    assertFalse(favoriteService.isFavorite(restaurant, user));
  }
}
