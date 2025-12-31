package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@WebMvcTest(HomeController.class)
public class HomeControllerUnitTest {

	@MockBean
	private RestaurantService restaurantService;

	@MockBean
	private CategoryService categoryService;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl adminUserDetails;
	private UserDetailsImpl regularUserDetails;
	private Category washokuCategory;
	private Category udonCategory;
	private Category donCategory;
	private Category ramenCategory;
	private Category odenCategory;
	private Category friedCategory;

	@BeforeEach
	public void setUp() {
		User adminUser = new User();
		Role adminRole = new Role();
		adminRole.setName("ROLE_ADMIN");
		adminUser.setRole(adminRole);
		adminUserDetails = new UserDetailsImpl(adminUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

		User regularUser = new User();
		Role userRole = new Role();
		userRole.setName("ROLE_FREE_MEMBER");
		regularUser.setRole(userRole);
		regularUserDetails = new UserDetailsImpl(regularUser, List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

		washokuCategory = new Category();
		washokuCategory.setName("和食");
		udonCategory = new Category();
		udonCategory.setName("うどん");
		donCategory = new Category();
		donCategory.setName("丼物");
		ramenCategory = new Category();
		ramenCategory.setName("ラーメン");
		odenCategory = new Category();
		odenCategory.setName("おでん");
		friedCategory = new Category();
		friedCategory.setName("揚げ物");
	}

	@Test
	@Description("GET / 管理者としてログイン済みの場合は管理画面にリダイレクトされる")
	public void index_test_1() throws Exception {
		mockMvc.perform(get("/").with(user(adminUserDetails)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin"));
	}

	@Test
	@Description("GET / 一般ユーザーとしてログイン済みの場合はトップページが正しく表示される")
	public void index_test_2() throws Exception {
		Page<Restaurant> highlyRatedRestaurants = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 6), 0);
		Page<Restaurant> newRestaurants = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 6), 0);
		List<Category> categories = List.of(washokuCategory, udonCategory);

		when(restaurantService.findAllRestaurantsByOrderByAverageScoreDesc(PageRequest.of(0, 6)))
				.thenReturn(highlyRatedRestaurants);
		when(restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(PageRequest.of(0, 6)))
				.thenReturn(newRestaurants);
		when(categoryService.findFirstCategoryByName("和食")).thenReturn(washokuCategory);
		when(categoryService.findFirstCategoryByName("うどん")).thenReturn(udonCategory);
		when(categoryService.findFirstCategoryByName("丼物")).thenReturn(donCategory);
		when(categoryService.findFirstCategoryByName("ラーメン")).thenReturn(ramenCategory);
		when(categoryService.findFirstCategoryByName("おでん")).thenReturn(odenCategory);
		when(categoryService.findFirstCategoryByName("揚げ物")).thenReturn(friedCategory);
		when(categoryService.findAllCategories()).thenReturn(categories);

		mockMvc.perform(get("/").with(user(regularUserDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("index"))
				.andExpect(model().attribute("highlyRatedRestaurants", highlyRatedRestaurants))
				.andExpect(model().attribute("newRestaurants", newRestaurants))
				.andExpect(model().attribute("washoku", washokuCategory))
				.andExpect(model().attribute("udon", udonCategory))
				.andExpect(model().attribute("don", donCategory))
				.andExpect(model().attribute("ramen", ramenCategory))
				.andExpect(model().attribute("oden", odenCategory))
				.andExpect(model().attribute("fried", friedCategory))
				.andExpect(model().attribute("categories", categories));

		PageRequest expectedHighlyRatedPageRequest = PageRequest.of(0, 6);
		PageRequest expectedNewPageRequest = PageRequest.of(0, 6);
		verify(restaurantService).findAllRestaurantsByOrderByAverageScoreDesc(expectedHighlyRatedPageRequest);
		verify(restaurantService).findAllRestaurantsByOrderByCreatedAtDesc(expectedNewPageRequest);
		verify(categoryService).findFirstCategoryByName("和食");
		verify(categoryService).findFirstCategoryByName("うどん");
		verify(categoryService).findFirstCategoryByName("丼物");
		verify(categoryService).findFirstCategoryByName("ラーメン");
		verify(categoryService).findFirstCategoryByName("おでん");
		verify(categoryService).findFirstCategoryByName("揚げ物");
		verify(categoryService).findAllCategories();
	}
}
