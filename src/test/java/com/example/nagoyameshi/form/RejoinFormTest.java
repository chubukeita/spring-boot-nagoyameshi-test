package com.example.nagoyameshi.form;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;

public class RejoinFormTest {

	RejoinForm rejoinForm = new RejoinForm();

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致すればtrueを返すこと")
	public void isSamePassword_test_1() {
		rejoinForm.setPassword("aaa");
		rejoinForm.setPasswordConfirmation("aaa");

		assertTrue(rejoinForm.isSamePassword());
	}

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致しなければfalseを返すこと")
	public void isSamePassword_test_2() {
		rejoinForm.setPassword("aaa");
		rejoinForm.setPasswordConfirmation("bbb");

		assertFalse(rejoinForm.isSamePassword());
	}

}
