package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.ResetToken;
import com.example.nagoyameshi.repository.ResetTokenRepository;
import com.example.nagoyameshi.service.ResetTokenService;

@ExtendWith(MockitoExtension.class)
public class ResetTokenServiceTest {

	@InjectMocks
	ResetTokenService resetTokenService;

	@Mock
	ResetTokenRepository resetTokenRepository;

	@Test
	@Description("createResetToken : パスワードリセット用トークンを生成し保存できる（正常系）")
	public void createResetToken_test_1() {

		ResetToken expectedResetToken = new ResetToken();

		String email = "taro.samurai@example.com";
		String token = "token";

		expectedResetToken.setEmail(email);
		expectedResetToken.setToken(token);

		resetTokenService.createResetToken(email, token);

		verify(resetTokenRepository, times(1)).save(expectedResetToken);
	}

	@Test
	@Description("deleteByToken : 指定したトークンを削除できる（正常系）")
	public void deleteByToken_test1() {
		resetTokenService.deleteByToken("token");
		verify(resetTokenRepository, times(1)).deleteByToken("token");
	}

	@Test
	@Description("findResetTokenByToken : トークン文字列からResetTokenを取得できる（正常系）")
	public void findResetTokenByToken_test1() {
		resetTokenService.findResetTokenByToken("token");
		verify(resetTokenRepository, times(1)).findByToken("token");
	}
}
