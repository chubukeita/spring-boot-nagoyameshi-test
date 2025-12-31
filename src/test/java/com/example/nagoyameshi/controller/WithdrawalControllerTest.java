package com.example.nagoyameshi.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WithdrawalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	// --------------------
	// GET /withdrawal/delete
	// --------------------

	@Test
	@Description("未ログイン：退会確認画面は login にリダイレクトされる")
	public void getDelete_anonymous_redirectToLogin() throws Exception {
		mockMvc.perform(get("/withdrawal/delete"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Description("ログイン中：退会確認画面が表示される")
	public void getDelete_loggedIn_showConfirm() throws Exception {
		mockMvc.perform(get("/withdrawal/delete"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"));
	}

	// --------------------
	// POST /withdrawal/delete
	// --------------------

	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Description("存在しないユーザーID：退会確認画面に戻る")
	public void postDelete_invalidUserId_showConfirm() throws Exception {
		mockMvc.perform(
				post("/withdrawal/delete/{id}", 9999)
						.with(csrf())
						.param("deleteReason", "test reason"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"));
	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Description("退会理由未入力：退会確認画面に戻る")
	public void postDelete_noReason_showConfirm() throws Exception {
		mockMvc.perform(
				post("/withdrawal/delete/{id}", 1)
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"));
	}

	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	@DisplayName("正常退会：ユーザー削除後 goodbye 画面が表示される")
	void postDelete_success_showGoodbye() throws Exception {
		User loginUser = userService.findUserByEmail("taro.samurai@example.com");

		mockMvc.perform(
				post("/withdrawal/delete/{id}", loginUser.getId())
						.with(csrf())
						.param("deleteReason", "サービスを利用しなくなったため"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/goodbye"));
	}

}
