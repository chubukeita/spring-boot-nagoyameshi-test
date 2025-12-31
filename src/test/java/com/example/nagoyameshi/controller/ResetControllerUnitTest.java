package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.PasswordResetRequestForm;
import com.example.nagoyameshi.service.ResetService;

@WebMvcTest(ResetController.class)
public class ResetControllerUnitTest {

	@MockBean
	private ResetService resetService;

	@MockBean
	private RedirectAttributes redirectAttributes;

	@Autowired
	private MockMvc mockMvc;

	// 共通で使うexpectedPasswordResetRequestForm
	public PasswordResetRequestForm createExpectedPasswordResetRequestForm(String email) {
		PasswordResetRequestForm expectedPasswordResetRequestForm = new PasswordResetRequestForm();

		expectedPasswordResetRequestForm.setEmail(email);
		return expectedPasswordResetRequestForm;
	}

	// 共通で使うexpectedPasswordResetForm
	public PasswordResetForm createExpectedPasswordResetForm(String token, String password,
			String passwordConfirmation) {
		PasswordResetForm expectedPasswordResetForm = new PasswordResetForm();

		expectedPasswordResetForm.setToken(token);
		expectedPasswordResetForm.setPassword(password);
		expectedPasswordResetForm.setPasswordConfirmation(passwordConfirmation);

		return expectedPasswordResetForm;
	}

	@WithMockUser
	@Test
	@Description("resetPassword_get_パスワード変更用の画面を表示できること")
	public void resetPassword_get_test_1() throws Exception {

		this.mockMvc.perform(get("/resetPassword"))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPassword"))
				.andExpect(model().attribute("passwordResetRequestForm", new PasswordResetRequestForm()));

	}

	private static final String BR_PASSWORD_RESET_REQUEST_FORM = BindingResult.MODEL_KEY_PREFIX
			+ "passwordResetRequestForm";

	@WithMockUser
	@Test
	@Description("resetPassword_post_入力したメールアドレスに誤りがなければ、会員用のトップページへリダイレクトされること")
	public void resetPassword_post_test_1() throws Exception {

		String email = "taro.samurai@example.com";

		PasswordResetRequestForm expectedPasswordResetRequestForm = createExpectedPasswordResetRequestForm(email);

		this.mockMvc.perform(post("/resetPassword")
				.with(csrf())
				.with(request -> {
					request.setScheme("http");
					request.setServerName("localhost");
					request.setServerPort(8080);
					return request;
				})
				.flashAttr("passwordResetRequestForm", expectedPasswordResetRequestForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(model().hasNoErrors())
				.andExpect(flash().attribute("successMessage",
						"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、パスワードの再設定を完了してください。"))
				.andExpect(model().attributeDoesNotExist(BR_PASSWORD_RESET_REQUEST_FORM));

		verify(resetService, times(1)).requestReset(eq(email), eq("http://localhost:8080/resetPassword"));
	}

	@WithMockUser
	@Test
	@Description("resetPassword_post_入力したメールアドレスに誤りがある場合、パスワード変更ページにエラーメッセージが表示されること")
	public void resetPassword_post_test_2() throws Exception {

		PasswordResetRequestForm expectedPasswordResetRequestForm = createExpectedPasswordResetRequestForm("");

		this.mockMvc.perform(post("/resetPassword")
				.with(csrf())
				.flashAttr("passwordResetRequestForm", expectedPasswordResetRequestForm))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPassword"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("passwordResetRequestForm", "email"))
				.andExpect(model().attributeExists("passwordResetRequestForm"))
				.andExpect(model().attribute("passwordResetRequestForm", expectedPasswordResetRequestForm))
				.andExpect(flash().attributeCount(0));

		verify(resetService, never()).requestReset(eq(""), eq("http://localhost:8080/resetPassword"));
	}

	@WithMockUser
	@Test
	@Description("resetPasswordVerification_get_トークンが有効なURLを開いた場合、パスワード再設定用ページが表示されること")
	public void resetPasswordVerification_get_test_1() throws Exception {

		String token = "valid-token";

		when(resetService.isValidToken(token)).thenReturn(true);

		this.mockMvc.perform(get("/resetPasswordVerification").param("token", token))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPasswordVerification"))
				.andExpect(model().attribute("token", token))
				.andExpect(model().attribute("successMessage", "メール認証にてご利用者様本人であることが確認できました。新しくパスワードを設定してください。"))
				.andExpect(model().attribute("passwordResetForm", new PasswordResetForm()))
				.andExpect(model().attribute("token", token));

		verify(resetService, times(1)).isValidToken(token);
	}

	@WithMockUser
	@Test
	@Description("resetPasswordVerification_get_トークンが無効なURLを開いた場合、エラーページが表示されること")
	public void resetPasswordVerification_get_test_2() throws Exception {

		String token = "invalid-token";

		when(resetService.isValidToken(token)).thenReturn(false);

		this.mockMvc.perform(get("/resetPasswordVerification").param("token", token))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/invalid"))
				.andExpect(model().attribute("errorMessage", "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。"));

		verify(resetService, times(1)).isValidToken(token);
	}

	@WithMockUser
	@Test
	@Description("resetPasswordVerification_post_バリデーションが問題なく正常に通った場合、パスワードを再設定してリダイレクトされること")
	public void resetPasswordVerification_post_test_1() throws Exception {

		String token = "valid-token";
		String newPassword = "password";

		PasswordResetForm expectedPasswordResetForm = createExpectedPasswordResetForm(token, newPassword, newPassword);

		this.mockMvc
				.perform(post("/resetPasswordVerification").with(csrf()).param("token", token)
						.param("password", newPassword).param("passwordConfirmation", newPassword))

				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("successMessage", "パスワードを再設定しました。"));

		verify(resetService, times(1)).resetPassword(token, expectedPasswordResetForm);
	}

	@WithMockUser
	@Test
	@Description("resetPasswordVerification_post_バリデーションで引っかかってエラーとなった場合、エラーメッセージが表示されること")
	public void resetPasswordVerification_post_test_2() throws Exception {

		String token = "valid-token";
		String newPassword = "";
		String validPassword = "";

		PasswordResetForm expectedPasswordResetForm = createExpectedPasswordResetForm(token, newPassword,
				validPassword);

		this.mockMvc
				.perform(post("/resetPasswordVerification").with(csrf()).param("token", token)
						.param("password", newPassword).param("passwordConfirmation", validPassword))

				.andExpect(status().isOk())
				.andExpect(view().name("auth/resetPasswordVerification"))
				.andExpect(model().attribute("token", token))
				.andExpect(model().hasErrors());

		verify(resetService, never()).resetPassword(token, expectedPasswordResetForm);
	}

}
