package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.PasswordResetRequestForm;
import com.example.nagoyameshi.service.ResetService;

@Controller
public class ResetController {

	private final ResetService resetService;

	public ResetController(ResetService resetService) {
		this.resetService = resetService;
	}

	@GetMapping("/resetPassword")
	public String resetPassword(Model model) {

		model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());

		return "auth/resetPassword";
	}

	@PostMapping("/resetPassword")
	public String resetPassword(@ModelAttribute @Validated PasswordResetRequestForm passwordResetRequestForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest,
			Model model) {

		if (bindingResult.hasErrors()) {
			return "auth/resetPassword";
		}

		resetService.requestReset(passwordResetRequestForm.getEmail(), httpServletRequest.getRequestURL().toString());

		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、パスワードの再設定を完了してください。");

		return "redirect:/";
	}

	@GetMapping("/resetPasswordVerification")
	public String resetPasswordVerification(@RequestParam("token") String token, Model model) {
		boolean valid = resetService.isValidToken(token);

		if (!valid) {
			// 「最初からやり直してください→ホームへ戻るボタン」の専用のページへ遷移させる(else文を消すときれいになる)、エラー時に再設定ボタンを押せてしまう。
			String errorMessage = "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。";
			model.addAttribute("errorMessage", errorMessage);
			return "auth/invalid";
		}

		String successMessage = "メール認証にてご利用者様本人であることが確認できました。新しくパスワードを設定してください。";

		// POSTに渡すためトークンをモデルに載せる
		model.addAttribute("token", token);
		model.addAttribute("successMessage", successMessage);
		model.addAttribute("passwordResetForm", new PasswordResetForm());

		return "auth/resetPasswordVerification";
	}

	@PostMapping("/resetPasswordVerification")
	public String resetPasswordVerification(@ModelAttribute @Validated PasswordResetForm passwordResetForm,
			BindingResult bindingResult,
			@RequestParam(name = "token") String token,
			RedirectAttributes redirectAttributes,
			Model model) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("token", token);
			return "auth/resetPasswordVerification";
		}

		try {
			resetService.resetPassword(token, passwordResetForm);
		} catch (com.example.nagoyameshi.service.error.PasswordMismatchException ex) {
			model.addAttribute("token", token);
			model.addAttribute("passwordResetForm", passwordResetForm);
			model.addAttribute("errorMessage", "パスワードが一致しません。");
			return "auth/resetPasswordVerification";
		}

		redirectAttributes.addFlashAttribute("successMessage", "パスワードを再設定しました。");
		return "redirect:/";

	}
}
