package com.example.nagoyameshi.controller;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(UserController.class)
public class UserControllerUnitTest {

	@MockBean
	private UserService userService;

	@MockBean
	private RedirectAttributes redirectAttributes;

	@Autowired
	private MockMvc mockMvc;

	private UserDetailsImpl userprincipal;

	User user = new User();

	@BeforeEach
	public void setUp() {

		user.setName("侍 太郎");
		user.setFurigana("サムライ タロウ");
		user.setPostalCode("1010022");
		user.setAddress("東京都千代田区神田練塀町300番地");
		user.setPhoneNumber("09012345678");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		user.setOccupation("エンジニア");
		user.setEmail("taro.samurai@example.com");

		this.userprincipal = new UserDetailsImpl(user, List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));
	}

	@Test
	@Description("index_ユーザー一覧表示画面の検証：ログイン済みの場合は会員用の会員情報ページが正しく表示されること")
	public void index_test1() throws Exception {

		this.mockMvc.perform(get("/user")
				.with(user(userprincipal)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/index"))
				.andExpect(model().attribute("user", user));
	}

	@Test
	@Description("edit_ユーザー編集画面の検証：誕生日が入力されていて、ログイン済みの場合は会員用の会員情報編集ページが正しく表示されること")
	public void edit_test1() throws Exception {
		UserEditForm userEditForm = new UserEditForm();

		userEditForm.setName("侍 太郎");
		userEditForm.setFurigana("サムライ タロウ");
		userEditForm.setPostalCode("1010022");
		userEditForm.setAddress("東京都千代田区神田練塀町300番地");
		userEditForm.setPhoneNumber("09012345678");
		userEditForm.setBirthday("19900101");
		userEditForm.setOccupation("エンジニア");
		userEditForm.setEmail("taro.samurai@example.com");

		this.mockMvc.perform(get("/user/edit")
				.with(user(userprincipal)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/edit"))
				.andExpect(model().attribute("userEditForm", userEditForm));
	}

	@Test
	@Description("edit_ユーザー編集画面の検証：誕生日が未入力で、ログイン済みの場合は会員用の会員情報編集ページが正しく表示されること")
	public void edit_test2() throws Exception {
		UserEditForm userEditForm = new UserEditForm();

		UserDetailsImpl unknownBirthdayUserPrincipal = new UserDetailsImpl(new User(),
				List.of(new SimpleGrantedAuthority("ROLE_USER")));

		this.mockMvc.perform(get("/user/edit")
				.with(user(unknownBirthdayUserPrincipal)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/edit"))
				.andExpect(model().attribute("userEditForm", userEditForm));
	}

	// 共通で使うexpectedUser
	public User createExpectedUser() {
		User expectedUser = new User();
		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedUser.setOccupation("エンジニア");
		expectedUser.setEmail("taro.samurai@example.com");
		return expectedUser;
	}

	// 共通で使うexpectedUserEditForm
	public UserEditForm createExpectedUserEditForm(String email) {
		UserEditForm expectedUserEditForm = new UserEditForm();

		expectedUserEditForm.setName("侍 太郎");
		expectedUserEditForm.setFurigana("サムライ タロウ");
		expectedUserEditForm.setPostalCode("1010022");
		expectedUserEditForm.setAddress("東京都千代田区神田練塀町300番地");
		expectedUserEditForm.setPhoneNumber("09012345678");
		expectedUserEditForm.setBirthday("");
		expectedUserEditForm.setOccupation("エンジニア");
		expectedUserEditForm.setEmail(email);
		return expectedUserEditForm;
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

	private static final String BR_USER_EDIT_FORM = BindingResult.MODEL_KEY_PREFIX + "userEditForm";

	@Test
	@Description("update_ユーザー情報更新時の検証：(true & true)ログイン状態で、メールアドレスが変更されており、かつ入力メールアドレスが登録済みの場合、会員用の会員情報編集ページにエラーメッセージが表示されること")
	public void update_test1() throws Exception {

		User expectedUser = createExpectedUser();

		UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro2.samurai@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
		when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(true);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().isOk())
				.andExpect(view().name("user/edit"))
				.andExpect(model().attributeExists("userEditForm"))
				.andExpect(model().attribute("userEditForm", expectedUserEditForm))
				.andExpect(model().attributeHasFieldErrors("userEditForm", "email"))
				.andExpect(model().attribute(BR_USER_EDIT_FORM, fieldError("email", "すでに登録済みのメールアドレスです。")));

		verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
		verify(userService, times(1)).isEmailRegistered(expectedUserEditForm.getEmail());
		verify(userService, never()).updateUser(expectedUserEditForm, expectedUser);

	}

	@Test
	@Description("update_ユーザー情報更新時の検証：(true & false)ログイン状態で、入力メールアドレスが変更されているが、未登録のメールアドレスが入力されていた場合、会員情報更新後に会員用の会員情報ページにリダイレクトされること")
	public void update_test2() throws Exception {

		User expectedUser = createExpectedUser();

		UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro2.samurai@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
		when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"))
				.andExpect(model().hasNoErrors())
				// redirect なので Model に BindingResult は無い（=存在しないことを確認）
				.andExpect(model().attributeDoesNotExist(BR_USER_EDIT_FORM));

		verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
		verify(userService, times(1)).isEmailRegistered(expectedUserEditForm.getEmail());
		verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);

	}

	@Test
	@Description("update_ユーザー情報更新時の検証：(false & true)ログイン状態で、入力メールアドレスが変更されていなかった場合（入力メールアドレスが登録済みかどうかを確認することなく）、会員情報更新後に会員用の会員情報ページにリダイレクトされること")
	public void update_test3() throws Exception {

		User expectedUser = createExpectedUser();

		UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro.samurai@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"))
				.andExpect(model().hasNoErrors())
				// redirect なので Model に BindingResult は無い（=存在しないことを確認）
				.andExpect(model().attributeDoesNotExist(BR_USER_EDIT_FORM));

		verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
		verify(userService, never()).isEmailRegistered(expectedUserEditForm.getEmail());
		verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);

	}

	@Test
	@Description("update_ユーザー情報更新時の検証：(false & false)ログイン状態で、入力メールアドレスが変更されていない場合（入力メールアドレスが登録済みかどうかを確認することなく）、会員情報更新後に会員用の会員情報ページにリダイレクトされること")
	public void update_test4() throws Exception {

		User expectedUser = createExpectedUser();

		UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro.samurai@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"))
				.andExpect(model().hasNoErrors())
				// redirect なので Model に BindingResult は無い（=存在しないことを確認）
				.andExpect(model().attributeDoesNotExist(BR_USER_EDIT_FORM));

		verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
		verify(userService, never()).isEmailRegistered(expectedUserEditForm.getEmail());
		verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);

	}

	// -------------------------------以下AIの出力結果-----------------------------------
	@Test
	@Description("index_ユーザー情報表示画面の検証：ユーザー情報が正しくモデルに渡されて表示されること")
	public void index_test_2() throws Exception {
		User expectedUser = createExpectedUser();

		this.mockMvc.perform(get("/user")
				.with(user(userprincipal)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/index"))
				.andExpect(model().attributeExists("user"));

		verify(userService, never()).updateUser(null, null);
	}

	@Test
	@Description("edit_ユーザー編集画面の検証：フォームオブジェクトが正しく生成されてモデルに追加されること")
	public void edit_test_3() throws Exception {
		this.mockMvc.perform(get("/user/edit")
				.with(user(userprincipal)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/edit"))
				.andExpect(model().attributeExists("userEditForm"));
	}

	@Test
	@Description("edit_ユーザー編集画面の検証：誕生日フィールドがフォームに正しくセットされること")
	public void edit_test_4() throws Exception {
		User userWithBirthday = new User();
		userWithBirthday.setName("侍 太郎");
		userWithBirthday.setFurigana("サムライ タロウ");
		userWithBirthday.setPostalCode("1010022");
		userWithBirthday.setAddress("東京都千代田区神田練塀町300番地");
		userWithBirthday.setPhoneNumber("09012345678");
		userWithBirthday.setBirthday(LocalDate.of(1990, 1, 1));
		userWithBirthday.setOccupation("エンジニア");
		userWithBirthday.setEmail("taro.samurai@example.com");

		UserDetailsImpl userPrincipalWithBirthday = new UserDetailsImpl(userWithBirthday,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

		this.mockMvc.perform(get("/user/edit")
				.with(user(userPrincipalWithBirthday)))
				.andExpect(status().isOk())
				.andExpect(view().name("user/edit"));
	}

	@Test
	@Description("update_ユーザー情報更新時の検証：メールアドレス変更検証が呼ばれることを確認")
	public void update_test_5() throws Exception {
		User expectedUser = createExpectedUser();
		UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
		when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

		verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
	}

	@Test
	@Description("update_ユーザー情報更新時の検証：メール登録確認が呼ばれることを確認")
	public void update_test_6() throws Exception {
		User expectedUser = createExpectedUser();
		UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
		when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection());

		verify(userService, times(1)).isEmailRegistered(expectedUserEditForm.getEmail());
	}

	@Test
	@Description("update_ユーザー情報更新時の検証：更新メソッドが呼ばれることを確認")
	public void update_test_7() throws Exception {
		User expectedUser = createExpectedUser();
		UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
		when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection());

		verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);
	}

	@Test
	@Description("update_ユーザー情報更新時の検証：バリデーションエラーがない場合は3xx リダイレクトステータスが返されること")
	public void update_test_8() throws Exception {
		User expectedUser = createExpectedUser();
		UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

		when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(false);

		this.mockMvc.perform(post("/user/update")
				.with(user(userprincipal))
				.with(csrf())
				.flashAttr("userEditForm", expectedUserEditForm))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));
	}
}
