package com.example.nagoyameshi.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.service.error.AlreadyEnabledException;
import com.example.nagoyameshi.service.error.InvalidTokenException;
import com.example.nagoyameshi.service.error.RejoinUserNotFoundException;
import com.example.nagoyameshi.service.error.UserNotFoundForTokenException;

@ControllerAdvice
public class GlobalControllerAdvice {

	@ExceptionHandler(InvalidTokenException.class)
	public String handleInvalidToken(InvalidTokenException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage", "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。");
		return "redirect:/resetPassword";
	}

	@ExceptionHandler(UserNotFoundForTokenException.class)
	public String handleUserNotFound(UserNotFoundForTokenException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage", "アカウントが見つかりません。");
		return "redirect:/resetPasswordVerification";
	}

	@ExceptionHandler(RejoinUserNotFoundException.class)
	public String handleRejoinUserNotFound(RejoinUserNotFoundException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/rejoin";
	}

	@ExceptionHandler(AlreadyEnabledException.class)
	public String handleAlreadyEnabled(AlreadyEnabledException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/rejoin";
	}

}
