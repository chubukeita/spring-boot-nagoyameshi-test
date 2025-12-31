package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

  @Mock
  private ReservationRepository reservationRepository;

  @InjectMocks
  private ReservationService reservationService;

  @Test
  @Description("findReservationById_IDで予約を取得し、Optionalで返すこと")
  public void findReservationById_test_1() {
    Reservation reservation = new Reservation();
    reservation.setId(1);
    when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

    Optional<Reservation> result = reservationService.findReservationById(1);

    assertTrue(result.isPresent());
    assertEquals(1, result.get().getId());
  }

  @Test
  @Description("findReservationsByUserOrderByReservedDatetimeDesc_ユーザーの予約一覧を予約日時の降順でページング取得できること")
  public void findReservationsByUserOrderByReservedDatetimeDesc_test_1() {
    User user = new User();
    Pageable pageable = PageRequest.of(0, 5);
    Page<Reservation> page = new PageImpl<>(java.util.List.of(new Reservation()), pageable, 1);
    when(reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable)).thenReturn(page);

    Page<Reservation> result = reservationService.findReservationsByUserOrderByReservedDatetimeDesc(user, pageable);

    assertEquals(1, result.getTotalElements());
    verify(reservationRepository).findByUserOrderByReservedDatetimeDesc(user, pageable);
  }

  @Test
  @Description("countReservations_予約件数を返すこと")
  public void countReservations_test_1() {
    when(reservationRepository.count()).thenReturn(3L);

    long count = reservationService.countReservations();

    assertEquals(3L, count);
    verify(reservationRepository).count();
  }

  @Test
  @Description("findFirstReservationByOrderByIdDesc_IDが最大の予約を取得できること")
  public void findFirstReservationByOrderByIdDesc_test_1() {
    Reservation reservation = new Reservation();
    reservation.setId(10);
    when(reservationRepository.findFirstByOrderByIdDesc()).thenReturn(reservation);

    Reservation result = reservationService.findFirstReservationByOrderByIdDesc();

    assertEquals(10, result.getId());
    verify(reservationRepository).findFirstByOrderByIdDesc();
  }

  @Test
  @Description("createReservation_フォームの内容から予約を作成して保存できること")
  public void createReservation_test_1() {
    ReservationRegisterForm form = new ReservationRegisterForm();
    LocalDate reservationDate = LocalDate.of(2050, 1, 1);
    LocalTime reservationTime = LocalTime.of(12, 0);
    form.setReservationDate(reservationDate);
    form.setReservationTime(reservationTime);
    form.setNumberOfPeople(4);

    Restaurant restaurant = new Restaurant();
    User user = new User();

    reservationService.createReservation(form, restaurant, user);

    Reservation expected = new Reservation();
    expected.setReservedDatetime(LocalDateTime.of(reservationDate, reservationTime));
    expected.setNumberOfPeople(4);
    expected.setRestaurant(restaurant);
    expected.setUser(user);

    verify(reservationRepository).save(expected);
  }

  @Test
  @Description("deleteReservation_指定した予約を削除できること")
  public void deleteReservation_delegatesDelete() {
    Reservation reservation = new Reservation();

    reservationService.deleteReservation(reservation);

    verify(reservationRepository).delete(reservation);
  }

  @Test
  @Description("isAtLeastTwoHoursInFuture_予約日時が現在時刻から2時間以上先ならtrue、それ未満ならfalseを返すこと")
  public void isAtLeastTwoHoursInFuture_checksFuture() {
    LocalDateTime twoHoursLater = LocalDateTime.now().plusHours(2);
    LocalDateTime oneHourLater = LocalDateTime.now().plusHours(1);

    assertTrue(reservationService.isAtLeastTwoHoursInFuture(twoHoursLater));
    assertFalse(reservationService.isAtLeastTwoHoursInFuture(oneHourLater));
  }
}
