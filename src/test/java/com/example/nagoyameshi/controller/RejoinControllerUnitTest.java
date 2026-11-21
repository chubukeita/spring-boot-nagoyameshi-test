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

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.errorMessage.RejoinResult;

@WebMvcTest(RejoinController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RejoinControllerUnitTest {

	@MockBean
	private UserService userService;

	@MockBean
	private RejoinService rejoinService;

	@MockBean
	private RejoinEventPublisher rejoinEventPublisher;

	@MockBean
	private RejoinTokenService rejoinTokenService;

	@MockBean
	private RedirectAttributes redirectAttributes;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl userprincipalActiveUser;
	private UserDetailsImpl userprincipalDeletedUser;

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

		this.userprincipalActiveUser = new UserDetailsImpl(expectedActiveUser,
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

		this.userprincipalDeletedUser = new UserDetailsImpl(expectedDeletedUser,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

	}

	public RejoinForm createExpectedRejoinForm(String email) {
		RejoinForm expectedRejoinForm = new RejoinForm();

		expectedRejoinForm.setEmail(email);
		expectedRejoinForm.setPassword("password");
		expectedRejoinForm.setPasswordConfirmation("password");

		return expectedRejoinForm;
	}

	public RejoinForm createExpectedPasswordInvalidRejoinForm(String email) {
		RejoinForm expectedPasswordInvalidRejoinForm = new RejoinForm();

		expectedPasswordInvalidRejoinForm.setEmail(email);
		expectedPasswordInvalidRejoinForm.setPassword("password");
		expectedPasswordInvalidRejoinForm.setPasswordConfirmation("password2");

		return expectedPasswordInvalidRejoinForm;
	}

	// RejoinResult上の成功時、失敗時の各定数

	String expectedSuccessMessage = "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。";
	String expectedSuccessButtonText = "ログインページへ";
	String expectedSuccessButtonUrl = "/login";

	int expectedErrorIdAlreadyEnabled = 1;
	int expectedErrorIdInvalidToken = 2;
	int expectedErrorIdUserNotFound = 3;

	String expectedErrorMessageAlreadyEnabled = "既にご利用中のアカウントです。";
	String expectedErrorMessageInvalidToken = "トークンが無効です。";
	String expectedErrorMessageUserNotFound = "該当メールアドレスのユーザーが見つかりません。";

	String expectedNextActionAlreadyEnabled = "現在入会中です。<br>以下のボタンより、ホーム画面に戻って<br>引き続き本サービスをご利用いただけます。";
	String expectedNextActionInvalidToken = "恐れ入りますが、<br>一度開いたURLを再度開くことはできないので、<br>再度メール認証からやり直してください。";
	String expectedNextActionUserNotFound = "恐れ入りますが、<br>登録したメールアドレスをご確認のうえ、<br>再度メール認証からやり直してください。";

	String expectedErrorButtonTextAlreadyEnabled = "ログイン画面へ戻る";
	String expectedErrorButtonUrlAlreadyEnabled = "/login";
	String expectedErrorButtonTextInvalidToken = "ホームへ戻る";
	String expectedErrorButtonUrlInvalidToken = "/";
	String expectedErrorButtonTextUserNotFound = "再入会画面に戻る";
	String expectedErrorButtonUrlUserNotFound = "/rejoin";

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

	private static final String BR_REJOIN_FORM = BindingResult.MODEL_KEY_PREFIX + "rejoinForm";

	@Test
	@Description("index_再入会画面の表示の検証：再入会画面を開くことができること")
	public void index_test1() throws Exception {
		// 1 given（どういう場合、設定を書く）
		RejoinForm rejoinForm = new RejoinForm();

		// 2 when（実行）
		this.mockMvc.perform(get("/rejoin")
				.with(csrf())
				.flashAttr("rejoinForm", rejoinForm))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/index"))
				.andExpect(model().attribute("rejoinForm", rejoinForm))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
	}

	@Test
	@Description("rejoin_再入会画面の検証：過去に解約・退会したユーザーが、正しくデータを入力した状態で再入会ボタンを押した時、認証成功時のメッセージ付きのトップ画面へリダイレクト遷移し、認証メールが送信されること。")
	public void rejoin_test1() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		String expectedUrl = "http://localhost/rejoin";

		// 解約ユーザーのテストデータの設定
		RejoinForm expectedRejoinForm = createExpectedRejoinForm(expectedEmail);

		User expectedRejoinUser = new User();

		expectedRejoinUser.setEmail(expectedRejoinForm.getEmail());
		expectedRejoinUser.setPassword(expectedRejoinForm.getPassword());

		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.of(expectedDeletedUser));
		when(rejoinService.rejoinUser(expectedRejoinForm, expectedDeletedUser)).thenReturn(expectedRejoinUser);
		doNothing().when(rejoinEventPublisher).publishRejoinEvent(expectedDeletedUser, expectedUrl);

		// 2 when（実行）
		this.mockMvc.perform(post("/rejoin")
				.with(csrf())
				.flashAttr("rejoinForm", expectedRejoinForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("successMessage",
						"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、再入会を完了してください。"))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(rejoinService, times(1)).rejoinUser(expectedRejoinForm, expectedDeletedUser);
		verify(rejoinEventPublisher, times(1)).publishRejoinEvent(expectedRejoinUser, expectedUrl);
	}

	@Test
	@Description("rejoin_再入会画面の検証：退会していないユーザー（例：現在会員、未登録のユーザー、ここでは現在会員）が、正しくデータを入力した状態で再入会ボタンを押した時、エラーメッセージを表示して、画面に留まること。")
	public void rejoin_test2() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		String expectedUrl = "http://localhost/rejoin";

		// 解約ユーザーのテストデータの設定
		RejoinForm expectedRejoinForm = createExpectedRejoinForm(expectedEmail);

		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.empty());

		// 2 when（実行）
		this.mockMvc.perform(post("/rejoin")
				.with(csrf())
				.flashAttr("rejoinForm", expectedRejoinForm))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/index"))
				.andExpect(model().attribute("errorMessage", "該当メールアドレスのユーザーが見つかりません。"))
				.andExpect(model().hasNoErrors()); // errorCount(1)出なくていいのか？bindingResultのエラーじゃないから大丈夫？？

		// 3 then（期待値）
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(rejoinService, never()).rejoinUser(any(), any());
		verify(rejoinEventPublisher, never()).publishRejoinEvent(any(), any());
	}

	@Test
	@Description("rejoin_再入会画面の検証：過去に解約・退会したユーザーが、パスワードが一致していない状態で再入会ボタンを押した時、エラーメッセージを表示して、画面に留まること。")
	public void rejoin_test3() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedEmail = "taro.samurai@example.com";
		String expectedUrl = "http://localhost/rejoin";

		// 解約ユーザーのテストデータの設定(パスワードとパスワード（確認用）で一致していない場合を想定)
		RejoinForm expectedPasswordInvalidRejoinForm = createExpectedPasswordInvalidRejoinForm(expectedEmail);

		when(userService.findDeletedUserByEmail(expectedEmail)).thenReturn(Optional.of(expectedDeletedUser));

		// 2 when（実行）
		this.mockMvc.perform(post("/rejoin")
				.with(csrf())
				.flashAttr("rejoinForm", expectedPasswordInvalidRejoinForm))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/index"))
				.andExpect(model().attribute(BR_REJOIN_FORM, fieldError("password", "パスワードが一致しません。")))
				.andExpect(model().errorCount(1)); // errorCount(1)出なくていいのか？bindingResultのエラーじゃないから大丈夫？？

		// 3 then（期待値）
		verify(userService, times(1)).findDeletedUserByEmail(expectedEmail);
		verify(rejoinService, never()).rejoinUser(any(), any());
		verify(rejoinEventPublisher, never()).publishRejoinEvent(any(), any());
	}

	@Test
	@Description("rejoin_verify_再入会完了画面の検証：一度退会済みのユーザーがメールに添付されたリンクを開いて認証が成功した際に、再入会完了画面へ遷移し、再入会完了のメッセージが表示されること。")
	public void rejoin_verify_test1() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedToken = "testtoken";

		RejoinToken rejoinToken = new RejoinToken();

		rejoinToken.setId(1);
		rejoinToken.setUser(expectedDeletedUser);
		rejoinToken.setToken(expectedToken);

		String expectedSuccessMessage = "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。";
		String expectedSuccessButtonText = "ログインページへ";
		String expectedSuccessButtonUrl = "/login";

		RejoinResult expectedResult = RejoinResult.success(expectedSuccessMessage, expectedSuccessButtonText,
				expectedSuccessButtonUrl);

		when(rejoinTokenService.findRejoinTokenByToken(expectedToken)).thenReturn(rejoinToken);
		when(rejoinService.rejoinValidate(expectedDeletedUser, expectedToken, rejoinToken))
				.thenReturn(expectedResult);
		doNothing().when(userService).enableUser(expectedDeletedUser);

		// 2 when（実行）
		this.mockMvc.perform(get("/rejoin/verify")
				.param("token", expectedToken)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/verify"))
				.andExpect(model().attribute("successMessage", expectedSuccessMessage))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
		verify(rejoinTokenService, times(1)).findRejoinTokenByToken(expectedToken);
		verify(rejoinService, times(1)).rejoinValidate(expectedDeletedUser, expectedToken, rejoinToken);
		verify(userService, times(1)).enableUser(expectedDeletedUser);
	}

	@Test
	@Description("rejoin_verify_再入会完了画面の検証：現在入会中のユーザーがメールに添付されたリンクを開いて認証が失敗した際に、再入会完了画面へ遷移し、既にアカウントが使われている旨のエラーメッセージが表示されること。")
	public void rejoin_verify_test2() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedToken = "testtoken";

		RejoinToken rejoinToken = new RejoinToken();

		rejoinToken.setId(1);
		rejoinToken.setUser(expectedActiveUser);
		rejoinToken.setToken(expectedToken);

		RejoinResult expectedResult = RejoinResult.error(expectedErrorIdAlreadyEnabled,
				expectedErrorMessageAlreadyEnabled,
				expectedNextActionAlreadyEnabled, expectedErrorButtonTextAlreadyEnabled,
				expectedErrorButtonUrlInvalidToken);

		when(rejoinTokenService.findRejoinTokenByToken(expectedToken)).thenReturn(rejoinToken);
		when(rejoinService.rejoinValidate(expectedActiveUser, expectedToken, rejoinToken))
				.thenReturn(expectedResult);

		// 2 when（実行）
		this.mockMvc.perform(get("/rejoin/verify")
				.param("token", expectedToken)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/verify"))
				.andExpect(model().attribute("errorMessage", expectedErrorMessageAlreadyEnabled))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
		verify(rejoinTokenService, times(1)).findRejoinTokenByToken(expectedToken);
		verify(rejoinService, times(1)).rejoinValidate(expectedActiveUser, expectedToken, rejoinToken);
		verify(userService, never()).enableUser(any());
	}

	@Test
	@Description("rejoin_verify_再入会完了画面の検証：一度退会済みのユーザーがメールに添付されたリンクを開いてtokenがなくて認証が失敗した際に、再入会完了画面へ遷移し、tokenが無効である旨のエラーメッセージが表示されること。")
	public void rejoin_verify_test3() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedToken = "testworngtoken";

		RejoinToken rejoinToken = new RejoinToken();

		rejoinToken.setId(1);
		rejoinToken.setUser(null);
		rejoinToken.setToken(expectedToken);

		RejoinResult expectedResult = RejoinResult.error(expectedErrorIdInvalidToken,
				expectedErrorMessageInvalidToken,
				expectedNextActionInvalidToken, expectedErrorButtonTextInvalidToken,
				expectedErrorButtonUrlInvalidToken);

		when(rejoinTokenService.findRejoinTokenByToken(expectedToken)).thenReturn(null);
		when(rejoinService.rejoinValidate(null, expectedToken, null))
				.thenReturn(expectedResult);

		// 2 when（実行）
		this.mockMvc.perform(get("/rejoin/verify")
				.param("token", expectedToken)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/verify"))
				.andExpect(model().attribute("errorMessage", expectedErrorMessageInvalidToken))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
		verify(rejoinTokenService, times(1)).findRejoinTokenByToken(expectedToken);
		verify(rejoinService, times(1)).rejoinValidate(null, expectedToken, null);
		verify(userService, never()).enableUser(any());
	}

	@Test
	@Description("rejoin_verify_再入会完了画面の検証：一度退会済みのユーザーがメールに添付されたリンクを開いてメールアドレスがなくて認証が失敗した際に、再入会完了画面へ遷移し、ユーザーが見つからない旨のエラーメッセージが表示されること。")
	public void rejoin_verify_test4() throws Exception {
		// 1 given（どういう場合、設定を書く）
		String expectedToken = "testtoken";

		RejoinToken rejoinToken = new RejoinToken();

		rejoinToken.setId(1);
		rejoinToken.setUser(null);
		rejoinToken.setToken(expectedToken);

		RejoinResult expectedResult = RejoinResult.error(expectedErrorIdUserNotFound,
				expectedErrorMessageUserNotFound,
				expectedNextActionUserNotFound, expectedErrorButtonTextUserNotFound,
				expectedErrorButtonUrlUserNotFound);

		when(rejoinTokenService.findRejoinTokenByToken(expectedToken)).thenReturn(rejoinToken);
		when(rejoinService.rejoinValidate(null, expectedToken, rejoinToken))
				.thenReturn(expectedResult);

		// 2 when（実行）
		this.mockMvc.perform(get("/rejoin/verify")
				.param("token", expectedToken)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("rejoin/verify"))
				.andExpect(model().attribute("errorMessage", expectedErrorMessageUserNotFound))
				.andExpect(model().hasNoErrors());

		// 3 then（期待値）
		verify(rejoinTokenService, times(1)).findRejoinTokenByToken(expectedToken);
		verify(rejoinService, times(1)).rejoinValidate(null, expectedToken, rejoinToken);
		verify(userService, never()).enableUser(any());
	}
}
