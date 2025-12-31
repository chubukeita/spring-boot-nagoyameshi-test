package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.TermService;

@WebMvcTest(AdminTermController.class)
public class AdminTermControllerUnitTest {

	@MockBean
	private TermService termService;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl adminUserDetails;
	private Term term;

	@BeforeEach
	public void setUp() {
		User adminUser = new User();
		Role adminRole = new Role();
		adminRole.setName("ROLE_ADMIN");
		adminUser.setRole(adminRole);

		adminUserDetails = new UserDetailsImpl(
				adminUser,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

		term = new Term();
		term.setId(1);
		term.setContent("initial term content");
	}

	@Test
	@Description("GET /admin/terms: 規約ページが表示され、term が model に入る")
	public void index_test_1() throws Exception {
		when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

		mockMvc.perform(get("/admin/terms").with(user(adminUserDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/terms/index"))
				.andExpect(model().attribute("term", term));

		verify(termService).findFirstTermByOrderByIdDesc();
	}

	@Test
	@Description("GET /admin/terms/edit: 編集ページが表示され、termEditForm が model に入る")
	public void edit_test_1() throws Exception {
		when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

		mockMvc.perform(get("/admin/terms/edit").with(user(adminUserDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/terms/edit"))
				.andExpect(model().attributeExists("termEditForm"));

		verify(termService).findFirstTermByOrderByIdDesc();
	}

	@Test
	@Description("POST /admin/terms/update: バリデーションエラーの場合は edit を返す")
	public void update_test_1() throws Exception {
		mockMvc.perform(post("/admin/terms/update")
				.with(user(adminUserDetails))
				.with(csrf())
				.param("content", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/terms/edit"));

		// バリデーションで止まる想定なので、サービス更新は呼ばれない
		verify(termService, never()).updateTerm(any(), any());
	}

	@Test
	@Description("POST /admin/terms/update: 正常更新の場合は /admin/terms にリダイレクトし successMessage が入る")
	public void update_test_2() throws Exception {
		when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

		String updatedContent = "updated term content";

		mockMvc.perform(post("/admin/terms/update")
				.with(user(adminUserDetails))
				.with(csrf())
				.param("content", updatedContent))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/terms"))
				.andExpect(flash().attribute("successMessage", "規約を更新しました。"));

		// 「フォームの中身」を equals に頼らず検証（安定）
		ArgumentCaptor<com.example.nagoyameshi.form.TermEditForm> formCaptor = ArgumentCaptor
				.forClass(com.example.nagoyameshi.form.TermEditForm.class);

		verify(termService).findFirstTermByOrderByIdDesc();
		verify(termService).updateTerm(formCaptor.capture(), eq(term));

		com.example.nagoyameshi.form.TermEditForm actualForm = formCaptor.getValue();
		// TermEditForm に getContent() がある想定。なければフィールド名に合わせて修正。
		org.junit.jupiter.api.Assertions.assertEquals(updatedContent, actualForm.getContent());
	}
}
