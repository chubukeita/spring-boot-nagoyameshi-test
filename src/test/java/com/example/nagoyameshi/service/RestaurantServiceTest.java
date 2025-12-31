package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
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
import com.example.nagoyameshi.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

  @Mock
  private RestaurantRepository restaurantRepository;

  @Mock
  private CategoryRestaurantService categoryRestaurantService;

  @Mock
  private RegularHolidayRestaurantService regularHolidayRestaurantService;

  @InjectMocks
  private RestaurantService restaurantService;

  @Test
  @Description("findAllRestaurantsByOrderByCreatedAtDesc_委譲して結果を返すこと")
  void findAllRestaurantsByOrderByCreatedAtDesc_test_1() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Restaurant> page = new PageImpl<>(List.of(new Restaurant()), pageable, 1);
    when(restaurantRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

    Page<Restaurant> result = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable);

    assertEquals(1, result.getTotalElements());
    verify(restaurantRepository).findAllByOrderByCreatedAtDesc(pageable);
  }

  @Test
  @Description("findRestaurantsByNameLike_キーワードで委譲して返すこと")
  void findRestaurantsByNameLike_test_1() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<Restaurant> page = new PageImpl<>(List.of(new Restaurant()), pageable, 1);
    when(restaurantRepository.findByNameLike("%寿司%", pageable)).thenReturn(page);

    Page<Restaurant> result = restaurantService.findRestaurantsByNameLike("寿司", pageable);

    assertEquals(1, result.getTotalElements());
    verify(restaurantRepository).findByNameLike("%寿司%", pageable);
  }

  @Test
  @Description("findRestaurantById_idで取得できること")
  void findRestaurantById_test_1() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(1);
    when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

    Optional<Restaurant> result = restaurantService.findRestaurantById(1);

    assertTrue(result.isPresent());
    assertEquals(1, result.get().getId());
  }

  @Test
  @Description("countRestaurants_件数を返すこと")
  void countRestaurants_test_1() {
    when(restaurantRepository.count()).thenReturn(5L);

    long count = restaurantService.countRestaurants();

    assertEquals(5L, count);
    verify(restaurantRepository).count();
  }

  @Test
  @Description("findFirstRestaurantByOrderByIdDesc_最新1件を返すこと")
  void findFirstRestaurantByOrderByIdDesc_test_1() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(3);
    when(restaurantRepository.findFirstByOrderByIdDesc()).thenReturn(restaurant);

    Restaurant result = restaurantService.findFirstRestaurantByOrderByIdDesc();

    assertEquals(3, result.getId());
    verify(restaurantRepository).findFirstByOrderByIdDesc();
  }

  @Test
  @Description("findAllRestaurantsByOrderByLowestPriceAsc_昇順で取得すること")
  void findAllRestaurantsByOrderByLowestPriceAsc_returnsPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Restaurant> page = new PageImpl<>(List.of(new Restaurant()), pageable, 1);
    when(restaurantRepository.findAllByOrderByLowestPriceAsc(pageable)).thenReturn(page);

    Page<Restaurant> result = restaurantService.findAllRestaurantsByOrderByLowestPriceAsc(pageable);

    assertEquals(1, result.getTotalElements());
    verify(restaurantRepository).findAllByOrderByLowestPriceAsc(pageable);
  }

  @Test
  @Description("deleteRestaurant_削除が委譲されること")
  void deleteRestaurant_delegatesDelete() {
    Restaurant restaurant = new Restaurant();

    restaurantService.deleteRestaurant(restaurant);

    verify(restaurantRepository).delete(restaurant);
  }

  @Test
  @Description("isValidPrices_上限が下限以上ならtrue")
  void isValidPrices_returnsTrueWhenHighGreater() {
    assertTrue(restaurantService.isValidPrices(1000, 2000));
    assertFalse(restaurantService.isValidPrices(2000, 1000));
  }

  @Test
  @Description("isValidBusinessHours_閉店が開店より後ならtrue")
  void isValidBusinessHours_validatesOrder() {
    assertTrue(restaurantService.isValidBusinessHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
    assertFalse(restaurantService.isValidBusinessHours(LocalTime.of(18, 0), LocalTime.of(9, 0)));
  }

  @Test
  @Description("generateNewFileName_拡張子を維持しつつファイル名を変えること")
  void generateNewFileName_changesBaseKeepsExtension() {
    String original = "image.png";

    String hashed = restaurantService.generateNewFileName(original);

    assertNotEquals(original, hashed);
    assertTrue(hashed.endsWith(".png"));
  }
}
