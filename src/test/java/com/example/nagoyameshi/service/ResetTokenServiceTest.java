package com.example.nagoyameshi.service;

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

@ExtendWith(MockitoExtension.class)
public class ResetTokenServiceTest {
	@InjectMocks
	ResetTokenService resetTokenService;

	@Mock
	ResetTokenRepository resetTokenRepository;

	@Test
	@Description("createResetToken_メールアドレスとトークンを設定したResetTokenを作成し、保存できること")
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
	@Description("deleteByToken_トークン指定でResetTokenを削除できること")
	public void deleteByToken_test1() {
		resetTokenService.deleteByToken("token");
		verify(resetTokenRepository, times(1)).deleteByToken("token");
	}

	@Test
	@Description("findResetTokenByToken_トークン指定でResetTokenを取得できること")
	public void findResetTokenByToken_test1() {
		resetTokenService.findResetTokenByToken("token");
		verify(resetTokenRepository, times(1)).findByToken("token");
	}
}
