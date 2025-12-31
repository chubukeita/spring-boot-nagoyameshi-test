package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.interceptor.BackLink;
import com.example.nagoyameshi.interceptor.StoreBack;
import com.example.nagoyameshi.record.AdminRestaurantListCond;
import com.example.nagoyameshi.service.CategoryRestaurantService;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RegularHolidayRestaurantService;
import com.example.nagoyameshi.service.RegularHolidayService;
import com.example.nagoyameshi.service.RestaurantNavService;
import com.example.nagoyameshi.service.RestaurantService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayService regularHolidayService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	private final RestaurantNavService restaurantNavService;

	public AdminRestaurantController(RestaurantService restaurantService, CategoryService categoryService,
			CategoryRestaurantService categoryRestaurantService,
			RegularHolidayService regularHolidayService,
			RegularHolidayRestaurantService regularHolidayRestaurantService,
			RestaurantNavService restaurantNavService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayService = regularHolidayService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
		this.restaurantNavService = restaurantNavService;
	}

	@StoreBack("admin/restaurants")
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			HttpServletRequest request,
			Model model) {

		Page<Restaurant> restaurantPage;

		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantService.findRestaurantsByNameLike(keyword, pageable);
		} else {
			restaurantPage = restaurantService.findAllRestaurants(pageable);

		}

		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);

		// 直前の一覧条件を保存（前へ/次へ で使う）
		request.getSession(true).setAttribute("ADMIN_REST_LAST_COND",
				new AdminRestaurantListCond(keyword));

		return "admin/restaurants/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttributes,
			HttpServletRequest request,
			Model model) {

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);

		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

			return "redirect:/admin/restaurants";
		}

		Restaurant restaurant = optionalRestaurant.get();

		model.addAttribute("restaurant", restaurant);

		// 戻るときのリンク情報を保持

		// 前後リンク用
		// varやめる（何でも取れるとバグの原因）
		var cond = (AdminRestaurantListCond) request.getSession().getAttribute("ADMIN_REST_LAST_COND");
		if (cond == null) {
			// 戻るときのリンク情報を保持
			String tmpBackUrl = BackLink.get(request, "admin/restaurants", "/admin/restaurants");
			var params = UriComponentsBuilder.fromUriString(tmpBackUrl).build().getQueryParams();

			String keyword = params.getFirst("keyword");

			cond = new AdminRestaurantListCond(keyword);

			// 必要ならここでセッションに入れておくと次回以降も安定
			request.getSession(true).setAttribute("ADMIN_REST_LAST_COND", cond);
		}

		var previewNext = restaurantNavService.findNeighborsNameOnly(
				id,
				cond.keyword());
		model.addAttribute("previewId", previewNext.previewId());
		model.addAttribute("nextId", previewNext.nextId());

		// 正しい「一覧に戻る」URLを現在IDから逆算して生成（ページまたぎ対応）

		int pageSize = 15; // 一覧の @PageableDefault(size=15) に合わせる
		String basePath = "/admin/restaurants";
		String backUrl = restaurantNavService.buildBackUrlForNameOnly(
				id,
				new AdminRestaurantListCond(cond.keyword()),
				pageSize,
				basePath);

		model.addAttribute("backUrl", backUrl);

		return "admin/restaurants/show";
	}

	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryService.findAllCategories();
		List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();

		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		model.addAttribute("categories", categories);
		model.addAttribute("regularHolidays", regularHolidays);

		return "admin/restaurants/register";
	}

	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {

		Integer lowestPrice = restaurantRegisterForm.getLowestPrice();
		Integer highestPrice = restaurantRegisterForm.getHighestPrice();
		LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
		LocalTime closingTime = restaurantRegisterForm.getClosingTime();

		if (lowestPrice != null && highestPrice != null
				&& !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice",
					"最低価格は最高価格以下に設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice",
					"最高価格は最低価格以上に設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);
		}

		if (openingTime != null && closingTime != null
				&& !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime",
					"開店時間は閉店時間よりも前に設定してください。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime",
					"閉店時間は開店時間よりも後に設定してください。");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);
		}

		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllCategories();
			List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();

			model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
			model.addAttribute("categories", categories);
			model.addAttribute("regularHolidays", regularHolidays);

			return "admin/restaurants/register";
		}

		restaurantService.createRestaurant(restaurantRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");

		return "redirect:/admin/restaurants";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttributes,
			Model model) {

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);

		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

			return "redirect:/admin/restaurants";
		}

		Restaurant restaurant = optionalRestaurant.get();
		List<Integer> categoryIds = categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
		List<Integer> regularHolidayIds = regularHolidayRestaurantService.findRegularHolidayIdsByRestaurant(restaurant);
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getName(),
				null,
				restaurant.getDescription(),
				restaurant.getLowestPrice(),
				restaurant.getHighestPrice(),
				restaurant.getPostalCode(),
				restaurant.getAddress(),
				restaurant.getOpeningTime(),
				restaurant.getClosingTime(),
				restaurant.getSeatingCapacity(),
				categoryIds,
				regularHolidayIds);

		List<Category> categories = categoryService.findAllCategories();
		List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();

		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("categories", categories);
		model.addAttribute("regularHolidays", regularHolidays);

		return "admin/restaurants/edit";

	}

	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm,
			BindingResult bindingResult,
			@PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttributes,
			Model model) {

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);

		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

			return "redirect:/admin/restaurants";
		}

		Restaurant restaurant = optionalRestaurant.get();
		Integer lowestPrice = restaurantEditForm.getLowestPrice();
		Integer highestPrice = restaurantEditForm.getHighestPrice();
		LocalTime openingTime = restaurantEditForm.getOpeningTime();
		LocalTime closingTime = restaurantEditForm.getClosingTime();

		if (lowestPrice != null && highestPrice != null
				&& !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice",
					"最低価格は最高価格以下に設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice",
					"最高価格は最低価格以上に設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);
		}

		if (openingTime != null && closingTime != null
				&& !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime",
					"開店時間は閉店時間よりも前に設定してください。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime",
					"閉店時間は開店時間よりも後に設定してください。");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);
		}

		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllCategories();
			List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();

			model.addAttribute("restaurant", restaurant);
			model.addAttribute("restaurantEditForm", restaurantEditForm);
			model.addAttribute("categories", categories);
			model.addAttribute("regularHolidays", regularHolidays);

			return "admin/restaurants/edit";
		}

		restaurantService.updateRestaurant(restaurantEditForm, restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を編集しました。");

		return "redirect:/admin/restaurants";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttributes) {

		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);

		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

			return "redirect:/admin/restaurants";
		}

		Restaurant restaurant = optionalRestaurant.get();
		restaurantService.deleteRestaurant(restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");

		return "redirect:/admin/restaurants";

	}

}
