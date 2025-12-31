package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

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
import org.springframework.data.domain.Sort;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.service.RestaurantNavService.PreviewNext;

@ExtendWith(MockitoExtension.class)
public class RestaurantNavServiceTest {

  @InjectMocks
  private RestaurantNavService restaurantNavService;

  @Mock
  private RestaurantRepository restaurantRepository;

  @Test
  @Description("findNeighborsNameOnly: 名前検索で前後の店舗IDを正しく取得できること")
  public void findNeighborsNameOnly_test_1() {
    Restaurant restaurant1 = new Restaurant();
    restaurant1.setId(1);
    Restaurant restaurant2 = new Restaurant();
    restaurant2.setId(2);
    Restaurant restaurant3 = new Restaurant();
    restaurant3.setId(3);

    List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2, restaurant3);
    Page<Restaurant> page = new PageImpl<>(restaurants);

    Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
    when(restaurantRepository.findByNameLike("%test%", expectedPageable)).thenReturn(page);

    PreviewNext result = restaurantNavService.findNeighborsNameOnly(2, "test");

    assertEquals(1, result.previewId());
    assertEquals(3, result.nextId());
    verify(restaurantRepository).findByNameLike("%test%", expectedPageable);
  }

  @Test
  @Description("findNeighborsNameOnly: 最初の店舗の場合はpreviewがnullになること")
  public void findNeighborsNameOnly_test_2() {
    Restaurant restaurant1 = new Restaurant();
    restaurant1.setId(1);
    Restaurant restaurant2 = new Restaurant();
    restaurant2.setId(2);

    List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);
    Page<Restaurant> page = new PageImpl<>(restaurants);

    Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
    when(restaurantRepository.findByNameLike("%", expectedPageable)).thenReturn(page);

    PreviewNext result = restaurantNavService.findNeighborsNameOnly(1, null);

    assertNull(result.previewId());
    assertEquals(2, result.nextId());
    verify(restaurantRepository).findByNameLike("%", expectedPageable);
  }

  @Test
  @Description("findNeighborsNameOnly: 最後の店舗の場合はnextがnullになること")
  public void findNeighborsNameOnly_test_3() {
    Restaurant restaurant1 = new Restaurant();
    restaurant1.setId(1);
    Restaurant restaurant2 = new Restaurant();
    restaurant2.setId(2);

    List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);
    Page<Restaurant> page = new PageImpl<>(restaurants);

    Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
    when(restaurantRepository.findByNameLike("%", expectedPageable)).thenReturn(page);

    PreviewNext result = restaurantNavService.findNeighborsNameOnly(2, null);

    assertEquals(1, result.previewId());
    assertNull(result.nextId());
    verify(restaurantRepository).findByNameLike("%", expectedPageable);
  }

  @Test
  @Description("findNeighborsNameOnly: 存在しないIDの場合はpreviewとnextがともにnullになること")
  public void findNeighborsNameOnly_returnsBothNullWhenIdNotFound() {
    Restaurant restaurant1 = new Restaurant();
    restaurant1.setId(1);
    Restaurant restaurant2 = new Restaurant();
    restaurant2.setId(2);

    List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);
    Page<Restaurant> page = new PageImpl<>(restaurants);

    Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
    when(restaurantRepository.findByNameLike("%", expectedPageable)).thenReturn(page);

    PreviewNext result = restaurantNavService.findNeighborsNameOnly(999, null);

    assertNull(result.previewId());
    assertNull(result.nextId());
    verify(restaurantRepository).findByNameLike("%", expectedPageable);
  }
}
