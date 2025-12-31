package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.record.RestaurantListCond;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantNavService;
import com.example.nagoyameshi.service.RestaurantService;

@WebMvcTest(RestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantControllerUnitTest {

	@MockBean
	private RestaurantService restaurantService;

	@MockBean
	private CategoryService categoryService;

	@MockBean
	private FavoriteService favoriteService;

	@MockBean
	private RestaurantNavService restaurantNavService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Description("GET /restaurants: パラメータなしで一覧取得できること")
	public void index_test_1() throws Exception {
		Pageable pageable = PageRequest.of(0, 15, Sort.by("id").ascending());
		Page<Restaurant> page = new PageImpl<>(List.of(createRestaurantStub()), pageable, 1);
		when(restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable)).thenReturn(page);
		when(categoryService.findAllCategories()).thenReturn(List.of(new Category()));

		mockMvc.perform(get("/restaurants"))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurants/index"))
				.andExpect(model().attributeExists("restaurantPage", "categories"));

		verify(restaurantService).findAllRestaurantsByOrderByCreatedAtDesc(pageable);
	}

	@Test
	@Description("GET /restaurants?keyword=寿司&order=lowestPriceAsc: キーワード検索で最低価格昇順を呼ぶこと")
	public void index_test_2() throws Exception {
		Pageable pageable = PageRequest.of(0, 15, Sort.by("id").ascending());
		Page<Restaurant> page = new PageImpl<>(List.of(createRestaurantStub()), pageable, 1);
		when(restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc("寿司",
				"寿司", "寿司", pageable)).thenReturn(page);
		when(categoryService.findAllCategories()).thenReturn(List.of());

		mockMvc.perform(get("/restaurants").param("keyword", "寿司").param("order", "lowestPriceAsc"))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurants/index"));

		verify(restaurantService)
				.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc("寿司", "寿司", "寿司",
						pageable);
	}

	@Test
	@Description("GET /restaurants/{id}: 該当なしはリダイレクトすること")
	public void show_test_1() throws Exception {
		when(restaurantService.findRestaurantById(99)).thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurants/99"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants"))
				.andExpect(flash().attributeExists("errorMessage"));
	}

	@Test
	@Description("GET /restaurants/{id}: 店舗が存在する場合に詳細を表示すること")
	public void show_test_2() throws Exception {
		Restaurant restaurant = createRestaurantStub();
		restaurant.setId(1);
		when(restaurantService.findRestaurantById(1)).thenReturn(Optional.of(restaurant));
		when(restaurantNavService.findNeighbors(1, null, null, null, "createdAtDesc"))
				.thenReturn(new RestaurantNavService.PreviewNext(null, null));
		when(restaurantNavService.buildBackUrlFor(eq(1), any(RestaurantListCond.class), eq(15), eq("/restaurants")))
				.thenReturn("/restaurants");

		mockMvc.perform(get("/restaurants/1")
				.sessionAttr("REST_LAST_COND", new RestaurantListCond(null, null, null, "createdAtDesc")))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurants/show"))
				.andExpect(model().attributeExists("restaurant", "backUrl"));

		verify(restaurantService).findRestaurantById(1);
	}

	private Restaurant createRestaurantStub() {
		Restaurant restaurant = new Restaurant();
		restaurant.setCategoriesRestaurants(List.of());
		restaurant.setRegularHolidaysRestaurants(List.of());
		restaurant.setReviews(List.of());
		return restaurant;
	}
}
