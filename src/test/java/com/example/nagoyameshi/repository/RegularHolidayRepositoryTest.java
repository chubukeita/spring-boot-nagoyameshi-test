package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.TestPropertySource;

import com.example.nagoyameshi.entity.RegularHoliday;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:regular_holiday_test;MODE=MySQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class RegularHolidayRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RegularHolidayRepository regularHolidayRepository;

  private RegularHoliday monday;
  private RegularHoliday tuesday;

  @BeforeEach
  void setUp() {
    monday = new RegularHoliday();
    monday.setDay("月");
    monday.setDayIndex(1);
    monday = entityManager.persist(monday);

    tuesday = new RegularHoliday();
    tuesday.setDay("火");
    tuesday.setDayIndex(2);
    tuesday = entityManager.persist(tuesday);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @Description("findAll_登録済みの定休日を取得できること")
  void findAll_test_1() {
    List<RegularHoliday> result = regularHolidayRepository.findAll();

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(r -> r.getDay().equals("月")));
    assertTrue(result.stream().anyMatch(r -> r.getDay().equals("火")));
  }
}
