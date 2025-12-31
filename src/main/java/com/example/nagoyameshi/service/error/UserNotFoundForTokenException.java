package com.example.nagoyameshi.service.error;

public class UserNotFoundForTokenException extends RuntimeException {
	public UserNotFoundForTokenException() {
		super("アカウントが見つかりません。");
	}
}
