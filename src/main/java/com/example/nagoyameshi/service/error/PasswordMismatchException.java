package com.example.nagoyameshi.service.error;

public class PasswordMismatchException extends RuntimeException {
	public PasswordMismatchException() {
		super("パスワードが一致しません。");
	}
}
