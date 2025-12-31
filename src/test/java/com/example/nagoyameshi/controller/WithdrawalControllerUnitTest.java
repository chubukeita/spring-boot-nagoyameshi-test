package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserWithdrawalForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(WithdrawalController.class)
public class WithdrawalControllerUnitTest {

	@MockBean
	private UserService userService;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl freeMemberDetails;
	private UserDetailsImpl paidMemberDetails;
	private User freeUser;
	private User paidUser;

	@BeforeEach
	public void setUp() {
		Role freeRole = new Role();
		freeRole.setId(1);
		freeRole.setName("ROLE_FREE_MEMBER");

		freeUser = new User();
		freeUser.setId(1);
		freeUser.setName("無料太郎");
		freeUser.setEmail("free@example.com");
		freeUser.setRole(freeRole);

		freeMemberDetails = new UserDetailsImpl(freeUser, List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

		Role paidRole = new Role();
		paidRole.setId(2);
		paidRole.setName("ROLE_PAID_MEMBER");

		paidUser = new User();
		paidUser.setId(2);
		paidUser.setName("有料花子");
		paidUser.setEmail("paid@example.com");
		paidUser.setRole(paidRole);

		paidMemberDetails = new UserDetailsImpl(paidUser, List.of(new SimpleGrantedAuthority("ROLE_PAID_MEMBER")));
	}

	@Test
	@Description("GET /withdrawal/delete 無料会員としてログイン済みの場合は退会確認画面が正しく表示される")
	public void showDeleteForm_test_1() throws Exception {
		mockMvc.perform(get("/withdrawal/delete").with(user(freeMemberDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"))
				.andExpect(model().attributeExists("userWithdrawalForm"))
				.andExpect(model().attribute("user", freeUser))
				.andExpect(model().attribute("roleName", "無料会員"));
	}

	@Test
	@Description("GET /withdrawal/delete 有料会員としてログイン済みの場合は退会確認画面が正しく表示される")
	public void showDeleteForm_test_2() throws Exception {
		mockMvc.perform(get("/withdrawal/delete").with(user(paidMemberDetails)))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"))
				.andExpect(model().attributeExists("userWithdrawalForm"))
				.andExpect(model().attribute("user", paidUser))
				.andExpect(model().attribute("roleName", "有料会員"));
	}

	@Test
	@Description("POST /withdrawal/delete/{id} ログイン中のユーザーIDとパスのIDが一致しない場合はエラーメッセージが表示される")
	public void delete_test_1() throws Exception {
		mockMvc.perform(post("/withdrawal/delete/999")
				.with(user(freeMemberDetails))
				.with(csrf())
				.param("deleteReason", "理由"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"))
				.andExpect(model().attribute("errorMessage", "不正なリクエストです。"));
	}

	@Test
	@Description("POST /withdrawal/delete/{id} バリデーションエラーがある場合は退会確認画面に戻る")
	public void delete_returnsToConfirmPageWhenValidationError() throws Exception {
		mockMvc.perform(post("/withdrawal/delete/1")
				.with(user(freeMemberDetails))
				.with(csrf())
				.param("deleteReason", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"));
	}

	@Test
	@Description("POST /withdrawal/delete/{id} ユーザーが見つからない場合はエラーメッセージが表示される")
	public void delete_showsErrorWhenUserNotFound() throws Exception {
		when(userService.findActiveByEmail("free@example.com")).thenReturn(Optional.empty());

		mockMvc.perform(post("/withdrawal/delete/1")
				.with(user(freeMemberDetails))
				.with(csrf())
				.param("deleteReason", "退会理由"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/confirm"))
				.andExpect(model().attribute("errorMessage", "ユーザーが見つかりません。"));

		verify(userService).findActiveByEmail("free@example.com");
	}

	@Test
	@Description("POST /withdrawal/delete/{id} 正常に退会処理が実行された場合は退会完了画面が表示される")
	public void delete_successfullyWithdraws() throws Exception {
		when(userService.findActiveByEmail("free@example.com")).thenReturn(Optional.of(freeUser));

		mockMvc.perform(post("/withdrawal/delete/1")
				.with(user(freeMemberDetails))
				.with(csrf())
				.param("deleteReason", "サービスを利用しなくなったため"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdrawal/goodbye"));

		UserWithdrawalForm expectedForm = new UserWithdrawalForm();
		expectedForm.setDeleteReason("サービスを利用しなくなったため");
		verify(userService).findActiveByEmail("free@example.com");
		verify(userService).withdrawal(any(UserWithdrawalForm.class), eq(freeUser), eq("サービスを利用しなくなったため"));
	}
}
