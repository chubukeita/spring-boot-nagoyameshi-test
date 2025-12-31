package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@ExtendWith(MockitoExtension.class)
public class RegularHolidayRestaurantServiceTest {

  @Mock
  private RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;

  @Mock
  private RegularHolidayService regularHolidayService;

  @InjectMocks
  private RegularHolidayRestaurantService regularHolidayRestaurantService;

  @Test
  @Description("findRegularHolidayIdsByRestaurant_店舗に紐づく定休日ID一覧を取得できること")
  public void findRegularHolidayIdsByRestaurant_test_1() {
    Restaurant restaurant = new Restaurant();
    when(regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(restaurant))
        .thenReturn(List.of(1, 2));

    List<Integer> result = regularHolidayRestaurantService.findRegularHolidayIdsByRestaurant(restaurant);

    assertEquals(List.of(1, 2), result);
    verify(regularHolidayRestaurantRepository).findRegularHolidayIdsByRestaurant(restaurant);
  }

  @Test
  @Description("createRegularHolidaysRestaurants_未登録の定休日のみを新規作成できること")
  public void createRegularHolidaysRestaurants_test_1() {
    Restaurant restaurant = new Restaurant();
    RegularHoliday monday = buildHoliday(1);

    when(regularHolidayService.findRegularHolidayById(1))
        .thenReturn(Optional.of(monday));
    when(regularHolidayRestaurantRepository
        .findByRegularHolidayAndRestaurant(monday, restaurant))
            .thenReturn(Optional.empty());

    List<Integer> ids = new ArrayList<>();
    ids.add(1);
    ids.add(null);

    regularHolidayRestaurantService.createRegularHolidaysRestaurants(ids, restaurant);

    RegularHolidayRestaurant expected = new RegularHolidayRestaurant();
    expected.setRegularHoliday(monday);
    expected.setRestaurant(restaurant);

    verify(regularHolidayRestaurantRepository).save(expected);
  }

  @Test
  @Description("syncRegularHolidaysRestaurants_定休日リストがnullの場合は全件削除されること")
  public void syncRegularHolidaysRestaurants_test_1() {
    Restaurant restaurant = new Restaurant();
    RegularHolidayRestaurant existing = new RegularHolidayRestaurant();

    when(regularHolidayRestaurantRepository.findByRestaurant(restaurant))
        .thenReturn(List.of(existing));

    regularHolidayRestaurantService.syncRegularHolidaysRestaurants(null, restaurant);

    verify(regularHolidayRestaurantRepository).delete(existing);
  }

  @Test
  @Description("syncRegularHolidaysRestaurants_差分に応じて削除と追加が行われること")
  public void syncRegularHolidaysRestaurants_test_2() {
    Restaurant restaurant = new Restaurant();

    RegularHoliday oldHoliday = buildHoliday(1);
    RegularHoliday newHoliday = buildHoliday(2);

    RegularHolidayRestaurant existing = new RegularHolidayRestaurant();
    existing.setRegularHoliday(oldHoliday);

    when(regularHolidayRestaurantRepository.findByRestaurant(restaurant))
        .thenReturn(List.of(existing));
    when(regularHolidayService.findRegularHolidayById(2))
        .thenReturn(Optional.of(newHoliday));
    when(regularHolidayRestaurantRepository
        .findByRegularHolidayAndRestaurant(newHoliday, restaurant))
            .thenReturn(Optional.empty());

    regularHolidayRestaurantService
        .syncRegularHolidaysRestaurants(List.of(2), restaurant);

    RegularHolidayRestaurant expected = new RegularHolidayRestaurant();
    expected.setRegularHoliday(newHoliday);
    expected.setRestaurant(restaurant);

    verify(regularHolidayRestaurantRepository).delete(existing);
    verify(regularHolidayRestaurantRepository).save(expected);
  }

  @Test
  @Description("createRegularHolidaysRestaurants_既に存在する定休日は登録されないこと")
  public void createRegularHolidaysRestaurants_test_2() {
    Restaurant restaurant = new Restaurant();
    RegularHoliday holiday = buildHoliday(1);
    RegularHolidayRestaurant already = new RegularHolidayRestaurant();

    when(regularHolidayService.findRegularHolidayById(1))
        .thenReturn(Optional.of(holiday));
    when(regularHolidayRestaurantRepository
        .findByRegularHolidayAndRestaurant(holiday, restaurant))
            .thenReturn(Optional.of(already));

    regularHolidayRestaurantService
        .createRegularHolidaysRestaurants(List.of(1), restaurant);

    verify(regularHolidayRestaurantRepository, never()).save(any());
  }

  private RegularHoliday buildHoliday(int id) {
    RegularHoliday rh = new RegularHoliday();
    rh.setId(id);
    rh.setDay("day" + id);
    rh.setDayIndex(id);
    return rh;
  }
}
