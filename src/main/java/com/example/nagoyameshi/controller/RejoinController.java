package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/rejoin")
public class RejoinController {
	private final UserService userService;
	private final RejoinService rejoinService;
	private final RejoinTokenService rejoinTokenService;

	public RejoinController(UserService userService, RejoinService rejoinService,
			RejoinTokenService rejoinTokenService) {
		this.userService = userService;
		this.rejoinService = rejoinService;
		this.rejoinTokenService = rejoinTokenService;
	}

	@GetMapping
	public String showRejonForm(Model model) {
		if (!model.containsAttribute("rejoinForm")) {
			model.addAttribute("rejoinForm", new RejoinForm());
		}
		return "rejoin/rejoin";
	}

	@PostMapping
	public String Rejoin(@ModelAttribute @Valid RejoinForm rejoinForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest,
			Model model) {

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("rejoinForm", rejoinForm);
			return "rejoin/rejoin";
		}

		try {
			rejoinService.requestRejoin(rejoinForm.getEmail(), httpServletRequest.getRequestURL().toString());
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("rejoinForm", rejoinForm);
			redirectAttributes.addFlashAttribute("errorMessage", "再入会対象のユーザーが見つかりませんでした。");
			return "redirect:/rejoin";
		}
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、再入会を完了してください。");

		return "redirect:/";
	}

	@GetMapping("/verify")
	public String verify(@RequestParam(name = "token") String token, Model model) {
		RejoinToken rejoinToken = rejoinTokenService.findRejoinTokenByToken(token);

		if (rejoinToken == null) {
			String errorMessage = "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。";
			model.addAttribute("errorMessage", errorMessage);
			return "auth/invalid";
		}

		rejoinService.rejoin(token);
		String successMessage = "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。";
		model.addAttribute("successMessage", successMessage);

		return "rejoin/verify";
	}
}
