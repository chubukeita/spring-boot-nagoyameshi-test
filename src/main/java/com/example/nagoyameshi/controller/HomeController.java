package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class HomeController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;

	public HomeController(RestaurantService restaurantService, CategoryService categoryService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}

	@GetMapping("/")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model) {

		if (userDetailsImpl != null && userDetailsImpl.getUser().getRole().getName().equals("ROLE_ADMIN")) {
			return "redirect:/admin";
		}

		Page<Restaurant> highlyRatedRestaurants = restaurantService
				.findAllRestaurantsByOrderByAverageScoreDesc(PageRequest.of(0, 6));
		Page<Restaurant> newRestaurants = restaurantService
				.findAllRestaurantsByOrderByCreatedAtDesc(PageRequest.of(0, 6));

		// "和食"等の固定値で入っていて、かつ、カテゴリーのデータなので、マスタデータであり、トランザクションデータではない。
		// よって、washokuなどの変数にnullが入ってくる可能性は極めて低い
		// なので、washokuという変数に対して、nullぽ対策は不要。
		// なお、固定値で入っていて、カテゴリーデータという情報だけで、データの細かい情報を知らなくてもこれらの情報だけでマスタデータと判断できる。細かいデータ構造を追う必要はない。

		// modelという変数も通常Springで管理されている変数であり、nullが入ってくる可能性はないので、
		// nullぽ対策がなくていい。

		// ×washokuという変数に対してメソッドを呼び出すことがないからnullぽ対策が不要というわけではない点に注意する。
		// 〇あくまでもwashokuにnullが入ってくる可能性がない→だから、nullぽ対策が不要というだけの話。

		// 一方、UserDetailsServiceImplクラスのloadUserByUsernameメソッドの中にある変数 userは、
		// メールアドレスでフォームから入力される値であり、マスタデータではなく、トランザクションデータであり、
		// 変数 user に対してnullが入ってくる可能性が想定される→だからnullぽ対策が必要である。

		// nullぽ対策が必要かどうかは、その変数がnullになりえる可能性があるのかを基準に考えることが重要となる。
		Category washoku = categoryService.findFirstCategoryByName("和食");
		Category udon = categoryService.findFirstCategoryByName("うどん");
		Category don = categoryService.findFirstCategoryByName("丼物");
		Category ramen = categoryService.findFirstCategoryByName("ラーメン");
		Category oden = categoryService.findFirstCategoryByName("おでん");
		Category fried = categoryService.findFirstCategoryByName("揚げ物");
		List<Category> categories = categoryService.findAllCategories();

		model.addAttribute("highlyRatedRestaurants", highlyRatedRestaurants);
		model.addAttribute("newRestaurants", newRestaurants);
		model.addAttribute("washoku", washoku);
		model.addAttribute("udon", udon);
		model.addAttribute("don", don);
		model.addAttribute("ramen", ramen);
		model.addAttribute("oden", oden);
		model.addAttribute("fried", fried);
		model.addAttribute("categories", categories);

		return "index";
	}
}
