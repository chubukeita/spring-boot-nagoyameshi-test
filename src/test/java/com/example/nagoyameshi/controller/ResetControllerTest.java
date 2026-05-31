package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.ResetToken;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.ResetEventPublisher;
import com.example.nagoyameshi.repository.ResetTokenRepository;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.ResetTokenService;

@SpringBootTest
@AutoConfigureMockMvc
// @ActiveProfiles("test")
@Transactional
@Sql("/test.sql")
public class ResetControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ResetTokenService resetTokenService;

	@Autowired
	private ResetTokenRepository resetTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// メール送信イベントは実際に投げない（副作用を避けるため）
	@MockBean
	private ResetEventPublisher resetEventPublisher;

	private User createUser(String email, String rawPassword) {
		Role free = roleRepository.findByName("ROLE_FREE_MEMBER");
		User u = new User();
		u.setName("テスト氏名");
		u.setFurigana("テストフリガナ");
		u.setPostalCode("0000000");
		u.setAddress("テスト住所");
		u.setPhoneNumber("00011112222");
		u.setBirthday(LocalDate.parse("20001111", DateTimeFormatter.ofPattern("yyyyMMdd")));
		u.setOccupation("テスト職業");
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode(rawPassword));
		u.setRole(free);
		u.setEnabled(true);
		return userRepository.save(u);
	}

	private ResetToken createToken(String email) {
		String token = UUID.randomUUID().toString();
		resetTokenService.createResetToken(email, token);
		return resetTokenRepository.findByToken(token);
	}

	@Test
	public void パスワード再設定_メール入力ページが表示される() throws Exception {
		mockMvc.perform(get("/resetPassword"))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPassword"))
				.andExpect(model().attributeExists("passwordResetRequestForm"));
	}

	@Test
	@Transactional
	public void パスワード再設定_メール送信リクエストでトップにリダイレクトしメッセージ表示() throws Exception {
		mockMvc.perform(post("/resetPassword")
				.with(csrf())
				.param("email", "taro.samurai@example.com"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attributeExists("successMessage"));
		// 送信自体はMockしているため、DBへの副作用はなし（OK）
	}

	@Test
	@Transactional
	public void パスワード再設定_有効なトークンでリセットページが表示される() throws Exception {
		User user = createUser("reset-ok@example.com", "oldpass123");
		ResetToken rt = createToken(user.getEmail());

		mockMvc.perform(get("/resetPasswordVerification").param("token", rt.getToken()))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPasswordVerification"))
				.andExpect(model().attributeExists("passwordResetForm"))
				.andExpect(model().attributeExists("token"))
				.andExpect(model().attributeExists("successMessage"));
	}

	@Test
	@Transactional
	public void パスワード再設定_無効なトークンでエラーメッセージが表示される() throws Exception {
		mockMvc.perform(get("/resetPasswordVerification").param("token", "invalid-token"))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/invalid"))
				.andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	@Transactional
	public void パスワード再設定_POST_成功するとパスワードが更新されトークンは削除される() throws Exception {
		// 事前にユーザー＆トークンを作成
		String email = "reset-success@example.com";
		String oldRaw = "oldPassword!";
		User user = createUser(email, oldRaw);
		ResetToken rt = createToken(email);

		// 変更前は旧パスが一致
		assertThat(passwordEncoder.matches(oldRaw, user.getPassword())).isTrue();

		String newRaw = "newPassword!";

		mockMvc.perform(post("/resetPasswordVerification")
				.with(csrf())
				.param("token", rt.getToken())
				.param("password", newRaw)
				.param("passwordConfirmation", newRaw))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attributeExists("successMessage"));

		// 再読込して新パスに変わっていることを確認
		User updated = userRepository.findByEmail(email);
		assertThat(passwordEncoder.matches(newRaw, updated.getPassword())).isTrue();

		// トークンは削除されていること
		ResetToken shouldBeNull = resetTokenRepository.findByToken(rt.getToken());
		assertThat(shouldBeNull).isNull();
	}

	@Test
	@Transactional
	public void パスワード再設定_POST_パスワード不一致なら更新せず画面に留まる() throws Exception {
		String email = "reset-mismatch@example.com";
		String oldRaw = "oldPassword!";
		User user = createUser(email, oldRaw);
		ResetToken rt = createToken(email);

		mockMvc.perform(post("/resetPasswordVerification")
				.with(csrf())
				.param("token", rt.getToken())
				.param("password", "abc12345")
				.param("passwordConfirmation", "DIFFERENT"))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPasswordVerification"))
				.andExpect(model().attributeExists("passwordResetForm"))
				.andExpect(model().attributeExists("token"));

		// 旧パスのまま
		User notUpdated = userRepository.findByEmail(email);
		assertThat(passwordEncoder.matches(oldRaw, notUpdated.getPassword())).isTrue();

		// トークンはまだ残っている
		assertThat(resetTokenRepository.findByToken(rt.getToken())).isNotNull();
	}

	@Test
	@Transactional
	public void パスワード再設定_POST_無効トークンならエラーでメール入力へリダイレクト() throws Exception {
		mockMvc.perform(post("/resetPasswordVerification")
				.with(csrf())
				.param("token", "invalid-token")
				.param("password", "abc12345")
				.param("passwordConfirmation", "abc12345"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/resetPassword"))
				.andExpect(flash().attributeExists("errorMessage"));
	}
}
