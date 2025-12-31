package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.repository.RegularHolidayRepository;

@ExtendWith(MockitoExtension.class)
public class RegularHolidayServiceTest {

  @Mock
  private RegularHolidayRepository regularHolidayRepository;

  @InjectMocks
  private RegularHolidayService regularHolidayService;

  @Test
  @Description("findRegularHolidayById_IDで定休日を取得できること（Optionalで返ること）")
  public void findRegularHolidayById_test_1() {
    RegularHoliday holiday = new RegularHoliday();
    holiday.setId(1);
    when(regularHolidayRepository.findById(1)).thenReturn(Optional.of(holiday));

    Optional<RegularHoliday> result = regularHolidayService.findRegularHolidayById(1);

    assertTrue(result.isPresent());
    assertEquals(1, result.get().getId());
  }

  @Test
  @Description("findAllRegularHolidays_定休日を全件取得できること")
  public void findAllRegularHolidays_test_1() {
    when(regularHolidayRepository.findAll()).thenReturn(List.of(new RegularHoliday()));

    List<RegularHoliday> result = regularHolidayService.findAllRegularHolidays();

    assertEquals(1, result.size());
    verify(regularHolidayRepository).findAll();
  }
}
