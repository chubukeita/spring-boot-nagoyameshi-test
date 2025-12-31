package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.interceptor.BackLink;
import com.example.nagoyameshi.interceptor.StoreBack;
import com.example.nagoyameshi.record.AdminUserListCond;
import com.example.nagoyameshi.service.UserNavService;
import com.example.nagoyameshi.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	private final UserService userService;
	private final UserNavService userNavService;

	public AdminUserController(UserService userService, UserNavService userNavService) {
		this.userService = userService;
		this.userNavService = userNavService;
	}

	@StoreBack("admin/users")
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			HttpServletRequest request,
			Model model) {

		Page<User> userPage;

		if (keyword != null && !keyword.isEmpty()) {
			userPage = userService.findUsersByNameLikeOrFuriganaLike(keyword, keyword, pageable);
		} else {
			userPage = userService.findAllUsers(pageable);
		}

		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);

		// 直前の一覧条件を保存（前へ/次へ で使う）
		request.getSession(true).setAttribute("ADMIN_USER_LAST_COND",
				new AdminUserListCond(keyword));

		return "admin/users/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttributes,
			HttpServletRequest request,
			Model model) {

		Optional<User> optionalUser = userService.findUserById(id);

		if (optionalUser.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが存在しません。");

			return "redirect:/admin/users";
		}

		User user = optionalUser.get();
		model.addAttribute("user", user);

		// 前後リンク用
		var cond = (AdminUserListCond) request.getSession().getAttribute("ADMIN_USER_LAST_COND");
		if (cond == null) {
			// 戻るときのリンク情報を保持
			String tmpBackUrl = BackLink.get(request, "admin/users", "/admin/users");
			var params = UriComponentsBuilder.fromUriString(tmpBackUrl).build().getQueryParams();

			String keyword = params.getFirst("keyword");

			cond = new AdminUserListCond(keyword);

			// 必要ならここでセッションに入れておくと次回以降も安定
			request.getSession(true).setAttribute("ADMIN_USER_LAST_COND", cond);
		}

		var previewNext = userNavService.findNeighborsNameOnly(
				id,
				cond.keyword(),
				cond.keyword());

		model.addAttribute("previewId", previewNext.previewId());
		model.addAttribute("nextId", previewNext.nextId());

		// 正しい「一覧に戻る」URLを現在IDから逆算して生成（ページまたぎ対応）

		int pageSize = 15; // 一覧の @PageableDefault(size=15) に合わせる
		String basePath = "/admin/users";
		String backUrl = userNavService.buildBackUrlForNameOnly(
				id,
				new AdminUserListCond(cond.keyword()),
				pageSize,
				basePath);

		model.addAttribute("backUrl", backUrl);
		return "admin/users/show";
	}

	private static Integer toInteger(String s) {
		try {
			return (s == null || s.isBlank()) ? null : Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}

	}
}
