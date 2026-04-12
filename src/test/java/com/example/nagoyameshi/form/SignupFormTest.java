package com.example.nagoyameshi.form;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;

public class SignupFormTest {

	SignupForm signupForm = new SignupForm();

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致すればtrueを返すこと")
	public void isSamePassword_test_1() {
		signupForm.setPassword("aaa");
		signupForm.setPasswordConfirmation("aaa");

		assertTrue(signupForm.isSamePassword());
	}

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致しなければfalseを返すこと")
	public void isSamePassword_test_2() {
		signupForm.setPassword("aaa");
		signupForm.setPasswordConfirmation("bbb");

		assertFalse(signupForm.isSamePassword());
	}
}
