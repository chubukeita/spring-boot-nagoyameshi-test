package com.example.nagoyameshi.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.errorMessage.RejoinResult;

@Controller
@RequestMapping("/rejoin")
public class RejoinController {
	private final UserService userService;
	private final RejoinService rejoinService;
	private final RejoinEventPublisher rejoinEventPublisher;
	private final RejoinTokenService rejoinTokenService;

	public RejoinController(UserService userService, RejoinService rejoinService,
			RejoinEventPublisher rejoinEventPublisher,
			RejoinTokenService rejoinTokenService) {
		this.userService = userService;
		this.rejoinService = rejoinService;
		this.rejoinEventPublisher = rejoinEventPublisher;
		this.rejoinTokenService = rejoinTokenService;
	}

	@GetMapping
	public String index(Model model) {
		model.addAttribute("rejoinForm", new RejoinForm());
		return "rejoin/index";
	}

	@PostMapping
	public String rejoin(@ModelAttribute @Valid RejoinForm rejoinForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest,
			Model model) {

		Optional<User> optionalUser = userService.findDeletedUserByEmail(rejoinForm.getEmail());

		// パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
		if (!rejoinForm.isSamePassword()) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("rejoinForm", rejoinForm);
			return "rejoin/index";
		}

		// 退会ユーザーが見つからない場合は、「該当メールアドレスのユーザーが見つかりません。」といったエラーメッセージを表示する
		if (optionalUser.isEmpty()) {
			RejoinResult result = RejoinResult.userNotFoundError();
			model.addAttribute("errorMessage", result.getErrorMessage());
			return "rejoin/index";
		}

		User user = optionalUser.get();

		User rejoinUser = rejoinService.rejoinUser(rejoinForm, user);
		String requestUrl = new String(httpServletRequest.getRequestURL());
		rejoinEventPublisher.publishRejoinEvent(rejoinUser, requestUrl);
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、再入会を完了してください。");

		return "redirect:/";
	}

	@GetMapping("/verify")
	public String verify(@RequestParam(name = "token") String token, Model model) {
		RejoinToken rejoinToken = rejoinTokenService.findRejoinTokenByToken(token);
		User user = (rejoinToken != null) ? rejoinToken.getUser() : null;

		RejoinResult result = rejoinService.rejoinValidate(user, token, rejoinToken);

		if (result.isSuccess()) {
			model.addAttribute("successMessage", result.getSuccessMessage());
			userService.enableUser(user);
		} else {
			model.addAttribute("errorId", result.getErrorId());
			model.addAttribute("errorMessage", result.getErrorMessage());
			model.addAttribute("nextActionMessage", result.getNextActionMessage());
		}

		model.addAttribute("buttonText", result.getButtonText());
		model.addAttribute("buttonUrl", result.getButtonUrl());
		return "rejoin/verify";
	}
}
