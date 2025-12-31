package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(RejoinController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RejoinControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private RejoinService rejoinService;

	@MockBean
	private RejoinTokenService rejoinTokenService;

	@Test
	public void 再入会フォーム画面が表示される() throws Exception {
		mockMvc.perform(get("/rejoin").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/rejoin"));
	}

	@Test
	public void 入力エラーがある場合は同画面に戻る() throws Exception {
		// email 未指定で @Valid エラー
		mockMvc.perform(post("/rejoin").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/rejoin"));
	}

	@Test
	public void 正常に再入会できた場合は確認画面を表示する() throws Exception {
		// テスト用ユーザーが test データに存在する前提（例：taro.samurai@example.com）
		mockMvc.perform(post("/rejoin")
				.with(csrf())
				.param("email", "taro.samurai@example.com"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attributeExists("successMessage"));

		verify(rejoinService).requestRejoin(eq("taro.samurai@example.com"), anyString());
	}

	@Test
	public void 対象ユーザーが存在しない場合は再入会画面にリダイレクトする() throws Exception {
		// 実サービスが IllegalArgumentException を投げる想定のメール
		doThrow(new IllegalArgumentException("not found")).when(rejoinService)
				.requestRejoin(eq("unknown.user@example.com"), anyString());
		mockMvc.perform(post("/rejoin")
				.with(csrf())
				.param("email", "unknown.user@example.com"))
				.andExpect(status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/rejoin"))
				.andExpect(flash().attributeExists("errorMessage"));
	}
}
