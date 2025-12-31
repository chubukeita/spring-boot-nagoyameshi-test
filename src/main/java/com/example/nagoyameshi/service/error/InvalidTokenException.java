package com.example.nagoyameshi.service.error;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException() {
		super("トークンが無効です。");
	}
}