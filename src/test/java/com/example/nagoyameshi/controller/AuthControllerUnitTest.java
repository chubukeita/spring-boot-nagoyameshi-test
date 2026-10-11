package com.example.nagoyameshi.controller;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
// @GetMapping("/login")のテストをおこなう際に、Spring Securityの機能を無効化しないと、/login?email=taro.samurai@email.comのように
// emailをクエリパラメータに付与してアクセスしても、/loginにリダイレクトされてしまい、emailのクエリパラメータがnullになってしまう事象を防ぐために、
// 追加した。
// ただ、今回行っているのは単体テストであり、結合テストではない。
// よって、外部機能である（SpringSecurity）の機能を@AutoConfigureMockMvc(addFilters = false)で無効化することで、
// 本当の意味で「単体」テストを実施することができる。SpringSecurityに関する単体テストは別クラスでテストコードとして記載する。
// なお、SpringSecurityの機能を無効化することで、.with(user(userprincipal)).with(csrf())の記載は不要となるが、仮に記載されていたとしても、
// SpringSecurityが無効化されているだけで、あっても使わない機能を、余計に持っているだけなので、明示的に削除しなくても問題ない。
public class AuthControllerUnitTest {

	@MockBean
	private UserService userService;

	@MockBean
	private SignupEventPublisher signupEventPublisher;

	@MockBean
	private VerificationTokenService verificationTokenService;

	@MockBean
	private RedirectAttributes redirectAttributes;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl userprincipal;

	User expectedActiveUser = new User();
	User expectedDeletedUser = new User();

	@BeforeEach
	public void setUp() {

		// 現在登録済みのユーザーのテストデータの設定
		expectedActiveUser.setName("侍 太郎");
		expectedActiveUser.setFurigana("サムライ タロウ");
		expectedActiveUser.setPostalCode("1010022");
		expectedActiveUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedActiveUser.setPhoneNumber("09012345678");
		expectedActiveUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedActiveUser.setOccupation("エンジニア");
		expectedActiveUser.setEmail("taro.samurai@example.com");
		expectedActiveUser.setPassword("password");
		expectedActiveUser.setRole(new Role());
		expectedActiveUser.setEnabled(true);
		expectedActiveUser.setDeletedAt(null);
		expectedActiveUser.setDeleteReason(null);
		expectedActiveUser.setDeletedByUser(null);

		this.userprincipal = new UserDetailsImpl(expectedActiveUser,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

		// 退会済みユーザーのテストデータの設定
		expectedDeletedUser.setName("侍 太郎");
		expectedDeletedUser.setFurigana("サムライ タロウ");
		expectedDeletedUser.setPostalCode("1010022");
		expectedDeletedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedDeletedUser.setPhoneNumber("09012345678");
		expectedDeletedUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedDeletedUser.setOccupation("エンジニア");
		expectedDeletedUser.setEmail("taro.samurai@example.com");
		expectedDeletedUser.setPassword("password");
		expectedDeletedUser.setRole(new Role());
		expectedDeletedUser.setEnabled(false);
		expectedDeletedUser.setDeletedAt(LocalDateTime.of(2026, 07, 01, 12, 00, 00));
		expectedDeletedUser.setDeleteReason("有料課金で高すぎる");
		expectedDeletedUser.setDeletedByUser(true);

		this.userprincipal = new UserDetailsImpl(expectedDeletedUser,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));
	}

	// 共通で使うSignupForm
	public SignupForm createExpectedSignupForm(String email) {
		SignupForm expectedSignupForm = new SignupForm();

		expectedSignupForm.setName("侍 太郎");
		expectedSignupForm.setFurigana("サムライ タロウ");
		expectedSignupForm.setPostalCode("1010022");
		expectedSignupForm.setAddress("東京都千代田区神田練塀町300番地");
		expectedSignupForm.setPhoneNumber("09012345678");
		expectedSignupForm.setBirthday("");
		expectedSignupForm.setOccupation("エンジニア");
		expectedSignupForm.setEmail(email);
		expectedSignupForm.setPassword("password");
		expectedSignupForm.setPasswordConfirmation("password");
		return expectedSignupForm;
	}

	public SignupForm createExpectedPasswordInvalidSignupForm(String email) {
		SignupForm expectedPasswordInvalidSignupForm = new SignupForm();

		expectedPasswordInvalidSignupForm.setName("侍 太郎");
		expectedPasswordInvalidSignupForm.setFurigana("サムライ タロウ");
		expectedPasswordInvalidSignupForm.setPostalCode("1010022");
		expectedPasswordInvalidSignupForm.setAddress("東京都千代田区神田練塀町300番地");
		expectedPasswordInvalidSignupForm.setPhoneNumber("09012345678");
		expectedPasswordInvalidSignupForm.setBirthday("");
		expectedPasswordInvalidSignupForm.setOccupation("エンジニア");
		expectedPasswordInvalidSignupForm.setEmail(email);
		expectedPasswordInvalidSignupForm.setPassword("password");
		expectedPasswordInvalidSignupForm.setPasswordConfirmation("password2");
		return expectedPasswordInvalidSignupForm;
	}

	public RejoinForm createExpectedRejoinForm(String email) {
		RejoinForm expectedRejoinForm = new RejoinForm();

		expectedRejoinForm.setEmail(email);
		expectedRejoinForm.setPassword("password");
		expectedRejoinForm.setPasswordConfirmation("password");

		return expectedRejoinForm;
	}

	/**
	 * Matcherを関数化
	 * BindingResult に、指定したフィールド名とメッセージを持つ FieldError が
	 * 1件以上含まれていることを検証する Matcher。
	 *
	 * 例：
	 *  - field = "email"
	 *  - message = "すでに登録済みのメールアドレスです。"
	 *
	 * → フィールド単位だけでなく、エラーメッセージの内容まで
	 *   厳密に一致していることを担保するために使用する。
	 */
	private Matcher<Object> fieldError(String field, String message) {
		return hasProperty("fieldErrors", hasItem(allOf(
				hasProperty("field", is(field)),
				hasProperty("defaultMessage", is(message)))));
	}

	private static final String BR_SIGNUP_FORM = BindingResult.MODEL_KEY_PREFIX + "signupForm";

	@Test
	@Description("signup_新規会員登録画面の検証：過去に解約・退会したユーザーが、新規会員登録画面から再入会しようとした場合は、再入会画面へ遷移すること。")
	public void signup_test1() throws Exception {
		String expectedEmail = "taro.samurai@example.com";

		SignupForm expectedSignupForm = createExpectedSignupForm(expectedEmail);
		RejoinForm expectedRejoinForm = createExpectedRejoinForm(expectedEmail);

		// bindingResultにエラーがないこともテストケースとして加える。
		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.empty());
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.of(expectedDeletedUser));
		//					when(userService.createUser(any())).thenReturn(null);
		// voidメソッドはdoNothing()を使える

		// signupEventPublisher.publishSignupEvent(expectedUser, expectedUrl)は呼ばれないことを検証するため、never()を使う
		// publishSignupEventはvoidのため、when().thenReturn()は使わない。

		// 期待値
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				// model().attribute("signupForm", expectedUserEditForm)
				.flashAttr("signupForm", expectedSignupForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/rejoin"))
				.andExpect(flash().attribute("rejoinForm", expectedRejoinForm))
				.andExpect(model().hasNoErrors());

		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(userService, never()).createUser(any());
		verify(signupEventPublisher, never()).publishSignupEvent(any(), any());
	}

	@Test
	@Description("signup_新規会員登録画面の検証：過去に解約していない新規ユーザーが、正しくデータを入力した状態で登録ボタンを押した時、認証成功時のメッセージ付きのログイン画面へ遷移し、認証メールが送信されること。")
	public void signup_test2() throws Exception {
		String expectedEmail = "taro.samurai@example.com";
		String expectedUrl = "http://localhost/signup";

		User expectedUndefinedUser = new User();

		// 新規ユーザーのテストデータの設定
		SignupForm expectedSignupForm = createExpectedSignupForm(expectedEmail);

		expectedUndefinedUser.setName(expectedSignupForm.getName());
		expectedUndefinedUser.setFurigana(expectedSignupForm.getFurigana());
		expectedUndefinedUser.setPostalCode(expectedSignupForm.getPostalCode());
		expectedUndefinedUser.setAddress(expectedSignupForm.getAddress());
		expectedUndefinedUser.setPhoneNumber(expectedSignupForm.getPhoneNumber());
		expectedUndefinedUser.setBirthday(null);
		expectedUndefinedUser.setOccupation(expectedSignupForm.getOccupation());
		expectedUndefinedUser.setEmail(expectedSignupForm.getEmail());
		expectedUndefinedUser.setPassword(expectedSignupForm.getPassword());

		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.empty());
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());
		when(userService.createUser(expectedSignupForm)).thenReturn(expectedUndefinedUser);
		// voidメソッドはdoNothing()を使える

		// signupEventPublisher.publishSignupEvent(expectedUser, expectedUrl)は呼ばれないことを検証するため、never()を使う
		// publishSignupEventはvoidのため、when().thenReturn()は使わない。

		// 期待値
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("signupForm", expectedSignupForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("successMessage",
						"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。"))
				.andExpect(model().hasNoErrors());
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(1)).createUser(expectedSignupForm);
		verify(signupEventPublisher, times(1)).publishSignupEvent(expectedUndefinedUser, expectedUrl);
	}

	@Test
	@Disabled("新規会員登録画面で正規ユーザーがログインしようとした場合の処理ですが、ログイン画面改修前には必要だったテストコード：現在不使用ですが比較のために残している")
	@Description("signup_新規会員登録画面の検証：メールアドレスが既に登録済み、かつパスワードが一致している場合は、新規会員登録画面のままエラーメッセージが表示されること。")
	public void signup_test3() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		SignupForm expectedSignupForm = createExpectedSignupForm(expectedEmail);

		User user = new User();
		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.of(user));
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());

		// 2 when（実行）
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("signupForm", expectedSignupForm))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/signup"))
				.andExpect(model().attribute(BR_SIGNUP_FORM, fieldError("email", "すでに登録済みのメールアドレスです。")))
				.andExpect(model().errorCount(1));

		// 3 then（期待値）
		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
	}

	@Test
	@Description("signup_新規会員登録画面の検証：過去に解約していない新規ユーザーが、異なるパスワードを入力して登録しようとした場合は、新規会員登録画面のままエラーメッセージが表示されること。")
	public void signup_test4() throws Exception {
		String expectedEmail = "taro.samurai@example.com";

		// 新規ユーザーのテストデータの設定(パスワードを一致していない場合)
		SignupForm expectedPasswordInvalidSignupForm = createExpectedPasswordInvalidSignupForm(expectedEmail);

		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.empty());
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());

		// 期待値
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("signupForm", expectedPasswordInvalidSignupForm))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/signup"))
				.andExpect(model().attribute(BR_SIGNUP_FORM, fieldError("password", "パスワードが一致しません。")))
				.andExpect(model().errorCount(1));

		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(0)).createUser(any());
		verify(signupEventPublisher, times(0)).publishSignupEvent(any(), any());
	}

	@Test
	@Disabled("新規会員登録画面で正規ユーザーがログインしようとした場合の処理ですが、ログイン画面改修前には必要だったテストコード：現在不使用ですが比較のために残している")
	@Description("signup_新規会員登録画面の検証：正規の登録ユーザー（退会ユーザーを除く）の場合は、メールアドレスが既に登録済み、かつパスワードが一致していない場合は、新規会員登録画面のままエラーメッセージが表示されること。")
	public void signup_test5() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		SignupForm expectedPasswordInvalidSignupForm = createExpectedPasswordInvalidSignupForm(expectedEmail);

		User user = new User();
		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.of(user));
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());

		// 2 when（実行）
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("signupForm", expectedPasswordInvalidSignupForm))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/signup"))
				.andExpect(model().attribute(BR_SIGNUP_FORM, fieldError("email", "すでに登録済みのメールアドレスです。")))
				.andExpect(model().attribute(BR_SIGNUP_FORM, fieldError("password", "パスワードが一致しません。")))
				.andExpect(model().errorCount(2));

		// 3 then（期待値）
		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(userService, times(0)).createUser(any());
		verify(signupEventPublisher, times(0)).publishSignupEvent(any(), any());
	}

	@Test
	@Description("signup_新規会員登録画面の検証：正規の登録ユーザー（退会ユーザーを除く）の場合は、メールアドレスが既に登録済みであれば、ログイン画面に遷移し、入力したメールアドレスをログイン画面のテキストボックスに表示されること。")
	public void signup_test6() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		SignupForm expectedSignupForm = createExpectedSignupForm(expectedEmail);

		User user = new User();
		when(userService.findActiveByEmail(expectedEmail)).thenReturn(Optional.of(user));
		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());

		// 2 when（実行）
		this.mockMvc.perform(post("/signup")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("signupForm", expectedSignupForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"))
				.andExpect(model().hasNoErrors())
				.andExpect(flash().attribute("email", expectedEmail))
				.andExpect(flash().attribute("errorMessage", "既に登録済みのメールアドレスです。ログインページよりログインしてください。"));

		// 3 then（期待値）
		verify(userService, times(1)).findActiveByEmail(expectedEmail);
		verify(userService, times(0)).findDeletedUserByEmail(expectedEmail);
		verify(userService, times(0)).createUser(any());
		verify(signupEventPublisher, times(0)).publishSignupEvent(any(), any());
	}

	@Test
	@Description("login_ログイン画面の検証：正規の登録ユーザー（退会ユーザーを除く）が新規登録画面からログインしようとした場合に、新規登録画面で入力されたメールアドレスを受け取り、ログイン画面のメールアドレステキストボックスに表示されること。")
	public void login_test1() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";

		// 2 when（実行）
		this.mockMvc.perform(get("/login")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("email", expectedEmail))
				.andExpect(status().isOk())
				.andExpect(view().name("auth/login"))
				.andExpect(model().attribute("email", expectedEmail))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
	}
}