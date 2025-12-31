package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserWithdrawalForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/withdrawal")
public class WithdrawalController {

	private final UserService userService;

	public WithdrawalController(UserService userService) {
		this.userService = userService;
	}

	/** 共通：削除確認画面に必要なモデルを詰める */
	private void populateDeletePageModel(Model model, User user) {
		Role role = user.getRole();
		String roleName = (role != null && role.getId() != null && role.getId() == 1) ? "無料会員" : "有料会員";

		if (!model.containsAttribute("userWithdrawalForm")) {
			model.addAttribute("userWithdrawalForm", new UserWithdrawalForm());
		}
		model.addAttribute("user", user);
		model.addAttribute("roleName", roleName);
	}

	/** GET: 削除確認画面 */
	@GetMapping("/delete")
	public String showDeleteForm(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
		User user = principal.getUser();
		populateDeletePageModel(model, user);
		return "withdrawal/confirm";
	}

	/** POST: 退会実行 */
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Integer id,
			@AuthenticationPrincipal UserDetailsImpl principal,
			@Valid @ModelAttribute UserWithdrawalForm userWithdrawalForm,
			BindingResult bindingResult,
			HttpServletRequest request,
			Model model) {

		// ① なりすまし防止：パスのIDとログイン中ユーザーのID一致チェック
		User loginUser = principal.getUser();
		if (loginUser == null || !loginUser.getId().equals(id)) {
			model.addAttribute("errorMessage", "不正なリクエストです。");
			// ビューに戻す前にモデルを再詰め
			populateDeletePageModel(model, principal.getUser());
			return "withdrawal/confirm";
		}

		// ② バリデーションエラー時：ビューに戻す前にモデルを再詰め
		// TODO 退会理由の文字制限、入力するまで非活性ボタンにする
		if (bindingResult.hasErrors()) {
			populateDeletePageModel(model, loginUser);
			return "withdrawal/confirm";
		}

		// ③ アクティブユーザー存在確認（メールから取得する現行仕様を尊重）
		Optional<User> optionalUser = userService.findActiveByEmail(loginUser.getEmail());
		if (!optionalUser.isPresent()) {
			model.addAttribute("errorMessage", "ユーザーが見つかりません。");
			populateDeletePageModel(model, loginUser);
			return "withdrawal/confirm";
		}

		// ④ 退会処理
		User user = optionalUser.get();
		userService.withdrawal(userWithdrawalForm, user, userWithdrawalForm.getDeleteReason());

		// ⑤ ログアウトして退会完了画面へ
		new SecurityContextLogoutHandler().logout(request, null, null);
		return "withdrawal/goodbye";
	}
}
