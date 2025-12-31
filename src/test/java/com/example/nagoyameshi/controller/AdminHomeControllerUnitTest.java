package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(AdminHomeController.class)
public class AdminHomeControllerUnitTest {

	@MockBean
	private UserService userService;

	@MockBean
	private RestaurantService restaurantService;

	@MockBean
	private ReservationService reservationService;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl adminUserDetails;

	@BeforeEach
	public void setUp() {
		User adminUser = new User();
		Role adminRole = new Role();
		adminRole.setName("ROLE_ADMIN");
		adminUser.setRole(adminRole);

		adminUserDetails = new UserDetailsImpl(
				adminUser,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	@Test
	@Description("GET /admin: 管理者トップページ（admin/index）を表示でき、各種集計値（会員数・店舗数・予約数・当月売上見込み）がmodelに設定されること")
	public void index_test_1() throws Exception {
		long totalFreeMembers = 100L;
		long totalPaidMembers = 50L;
		long totalRestaurants = 200L;
		long totalReservations = 150L;
		long expectedSalesForThisMonth = 300 * totalPaidMembers;

		when(userService.countUsersByRole_Name("ROLE_FREE_MEMBER")).thenReturn(totalFreeMembers);
		when(userService.countUsersByRole_Name("ROLE_PAID_MEMBER")).thenReturn(totalPaidMembers);
		when(restaurantService.countRestaurants()).thenReturn(totalRestaurants);
		when(reservationService.countReservations()).thenReturn(totalReservations);

		mockMvc.perform(get("/admin").with(user(adminUserDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/index"))
				.andExpect(model().attribute("totalFreeMembers", totalFreeMembers))
				.andExpect(model().attribute("totalPaidMembers", totalPaidMembers))
				.andExpect(model().attribute("totalMembers", totalFreeMembers + totalPaidMembers))
				.andExpect(model().attribute("totalRestaurants", totalRestaurants))
				.andExpect(model().attribute("totalReservations", totalReservations))
				.andExpect(model().attribute("salesForThisMonth", expectedSalesForThisMonth));

		verify(userService).countUsersByRole_Name("ROLE_FREE_MEMBER");
		verify(userService).countUsersByRole_Name("ROLE_PAID_MEMBER");
		verify(restaurantService).countRestaurants();
		verify(reservationService).countReservations();
	}
}
