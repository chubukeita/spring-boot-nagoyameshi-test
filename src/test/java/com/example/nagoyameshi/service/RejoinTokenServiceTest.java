package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.repository.RejoinTokenRepository;

@ExtendWith(MockitoExtension.class)
public class RejoinTokenServiceTest {

	@Mock
	private RejoinTokenRepository rejoinTokenRepository;

	@InjectMocks
	private RejoinTokenService rejoinTokenService;

	RejoinForm expectedRejoinForm = new RejoinForm();
	User expectedUser = new User();

	@BeforeEach
	public void setUp() {

		expectedRejoinForm.setEmail("taro.samurai@example.com");
		expectedRejoinForm.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");
		expectedRejoinForm.setPasswordConfirmation("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(LocalDate.parse("19900101", DateTimeFormatter.ofPattern("yyyyMMdd")));
		expectedUser.setOccupation("エンジニア");
		expectedUser.setEmail("taro.samurai@example.com");
		expectedUser.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");
		expectedUser.setRole(new Role());
		expectedUser.setEnabled(false);
	}

	@Test
	@Description("createRejoinToken_メールアドレスとトークンで再入会トークンを作成し保存すること")
	public void createRejoinToken_test_1() {
		rejoinTokenService.createRejoinToken(expectedUser, "token");

		RejoinToken rejoinToken = new RejoinToken();
		rejoinToken.setUser(expectedUser);
		rejoinToken.setToken("token");
		verify(rejoinTokenRepository).save(rejoinToken);
	}

	//	@Test
	//	@Description("deleteByToken_トークンを指定して削除処理が呼ばれること")
	//	public void deleteByToken_test_1() {
	//		rejoinTokenService.deleteByToken("token");
	//
	//		verify(rejoinTokenRepository).deleteByToken("token");
	//	}

	@Test
	@Description("findRejoinTokenByToken_トークンで検索して結果を返すこと")
	public void findRejoinTokenByToken_test_1() {
		RejoinToken token = new RejoinToken();
		when(rejoinTokenRepository.findByToken("token")).thenReturn(token);

		RejoinToken result = rejoinTokenService.findRejoinTokenByToken("token");

		assertEquals(token, result);
		verify(rejoinTokenRepository).findByToken("token");
	}
}
