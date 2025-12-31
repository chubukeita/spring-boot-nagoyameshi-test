package com.example.nagoyameshi.service.error;

public class RejoinUserNotFoundException extends RuntimeException {
	public RejoinUserNotFoundException() {
		super("該当メールアドレスのユーザーが見つかりません。");
	}
}
