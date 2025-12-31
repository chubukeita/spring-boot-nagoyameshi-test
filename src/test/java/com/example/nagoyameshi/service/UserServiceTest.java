package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.form.UserWithdrawalForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.AlreadyEnabledException;
import com.example.nagoyameshi.service.error.RejoinUserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@InjectMocks
	UserService userService;

	@Mock
	UserRepository userRepository;

	@Mock
	RoleRepository roleRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	private MockedStatic<LocalDateTime> localDateTimeMock;

	// 固定する時刻
	private final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2025, 10, 21, 9, 0, 0);

	@BeforeEach
	public void setUp() {
		// localDateTimeMockについて
		localDateTimeMock = mockStatic(LocalDateTime.class);
		// LocalDateTime.now()が呼ばれたらLOCAL_DATE_TIMEを返す
		localDateTimeMock.when(() -> LocalDateTime.now()).thenReturn(LOCAL_DATE_TIME);

		// authenticationについて
		// テスト用の認証情報を作成
		List<GrantedAuthority> initialAuthorities = List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER"));
		Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", initialAuthorities);

		// SecurityContextに設定
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@AfterEach
	public void close() {
		// localDateTimeMockについて
		localDateTimeMock.close();

		// authenticationについて
		SecurityContextHolder.clearContext();
	}

	@Test
	@Description("createUser: フォーム上で生年月日、職業がともに入力されている場合に、フォームから送信された会員情報をデータベースに登録できること")
	public void createUser_test_1() {

		SignupForm signupForm = new SignupForm();

		signupForm.setName("侍 太郎");
		signupForm.setFurigana("サムライ タロウ");
		signupForm.setPostalCode("1010022");
		signupForm.setAddress("東京都千代田区神田練塀町300番地");
		signupForm.setPhoneNumber("09012345678");
		signupForm.setBirthday("19900101");
		signupForm.setOccupation("エンジニア");
		signupForm.setEmail("taro.samurai@example.com");
		signupForm.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		User user2 = new User();
		user2.setName(signupForm.getName());
		user2.setFurigana(signupForm.getFurigana());
		user2.setPostalCode(signupForm.getPostalCode());
		user2.setAddress(signupForm.getAddress());
		user2.setPhoneNumber(signupForm.getPhoneNumber());
		user2.setBirthday(LocalDate.parse(signupForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		user2.setOccupation(signupForm.getOccupation());
		user2.setEmail(signupForm.getEmail());
		user2.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");
		user2.setRole(new Role());
		user2.setEnabled(false);

		// 実装のuserRepository.save(user);を実行した時に返されるUser型のインスタンスをそのままreturnするように指定する。
		// thenReturn(new
		// User()):としてしまうと、せっかくcreateUser()メソッドで、各種フィールドをセットしたuserが返されず、nullが返されてしまう。
		// returnされるときに、何かしらの処理が行われていれば、別のインスタンスを返す必要がある。
		when(userRepository.save(user2)).thenReturn(user2);
		when(roleRepository.findByName("ROLE_FREE_MEMBER")).thenReturn(new Role());
		when(passwordEncoder.encode(signupForm.getPassword()))
				.thenReturn("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		User user = userService.createUser(signupForm);
		verify(roleRepository, times(1)).findByName("ROLE_FREE_MEMBER");

		User expectedUser = new User();
		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedUser.setOccupation("エンジニア");
		expectedUser.setEmail("taro.samurai@example.com");
		expectedUser.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");
		expectedUser.setRole(new Role());
		expectedUser.setEnabled(false);

		verify(userRepository, times(1)).save(expectedUser);
		assertEquals(expectedUser, user);

	}

	@Test
	@Description("createUser: フォーム上で生年月日、職業が未入力の場合に、そのデータがデータベースに登録されないこと")
	public void createUser_test_2() {

		SignupForm signupForm = new SignupForm();

		signupForm.setName("侍 太郎");
		signupForm.setFurigana("サムライ タロウ");
		signupForm.setPostalCode("1010022");
		signupForm.setAddress("東京都千代田区神田練塀町300番地");
		signupForm.setPhoneNumber("09012345678");
		signupForm.setBirthday("");
		signupForm.setOccupation("");
		signupForm.setEmail("taro.samurai@example.com");
		signupForm.setPassword("password");

		// 実装のuserRepository.save(user);を実行した時に返されるUser型のインスタンスをそのままreturnするように指定する。
		// thenReturn(new
		// User()):としてしまうと、せっかくcreateUser()メソッドで、各種フィールドをセットしたuserが返されず、nullが返されてしまう。
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(roleRepository.findByName("ROLE_FREE_MEMBER")).thenReturn(new Role());
		when(passwordEncoder.encode(signupForm.getPassword()))
				.thenReturn("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		User user = userService.createUser(signupForm);
		verify(roleRepository, times(1)).findByName("ROLE_FREE_MEMBER");

		User expectedUser = new User();
		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(null);
		expectedUser.setOccupation("");
		expectedUser.setEmail("taro.samurai@example.com");
		expectedUser.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");
		expectedUser.setRole(new Role());
		expectedUser.setEnabled(false);

		verify(userRepository, times(1)).save(expectedUser);
		assertEquals(expectedUser, user);

	}

	@Test
	@Description("updateUser: フォーム上で生年月日、職業がともに入力されている場合に、フォームから送信された会員情報でデータベースを更新できること")
	public void updateUser_test_1() {

		UserEditForm userEditForm = new UserEditForm();
		User user = new User();

		userEditForm.setName("侍 太郎");
		userEditForm.setFurigana("サムライ タロウ");
		userEditForm.setPostalCode("1010022");
		userEditForm.setAddress("東京都千代田区神田練塀町300番地");
		userEditForm.setPhoneNumber("09012345678");
		userEditForm.setBirthday("19900101");
		userEditForm.setOccupation("エンジニア");
		userEditForm.setEmail("taro.samurai@example.com");

		userService.updateUser(userEditForm, user);

		User expectedUser = new User();

		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedUser.setOccupation("エンジニア");
		expectedUser.setEmail("taro.samurai@example.com");

		verify(userRepository, times(1)).save(expectedUser);

		assertEquals(expectedUser, user);

	}

	@Test
	@Description("updateUser: フォーム上で生年月日、職業が未入力の場合に、そのデータがデータベースに更新されないこと")
	public void updateUser_test_2() {

		UserEditForm userEditForm = new UserEditForm();
		User user = new User();

		userEditForm.setName("侍 太郎");
		userEditForm.setFurigana("サムライ タロウ");
		userEditForm.setPostalCode("1010022");
		userEditForm.setAddress("東京都千代田区神田練塀町300番地");
		userEditForm.setPhoneNumber("09012345678");
		userEditForm.setBirthday("");
		userEditForm.setOccupation("");
		userEditForm.setEmail("taro.samurai@example.com");

		userService.updateUser(userEditForm, user);

		User expectedUser = new User();

		expectedUser.setName("侍 太郎");
		expectedUser.setFurigana("サムライ タロウ");
		expectedUser.setPostalCode("1010022");
		expectedUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedUser.setPhoneNumber("09012345678");
		expectedUser.setBirthday(null);
		expectedUser.setOccupation(null);
		expectedUser.setEmail("taro.samurai@example.com");

		verify(userRepository, times(1)).save(expectedUser);

		assertEquals(expectedUser, user);

	}

	@Test
	@Description("isEmailRegistered: メールアドレスが登録済みならtrueを返すこと")
	public void isEmailRegistered_test_1() {
		String email = "taro.samurai@example.com";

		when(userRepository.findByEmail(email)).thenReturn(new User());

		assertTrue(userService.isEmailRegistered(email));

		verify(userRepository, times(1)).findByEmail(email);

	}

	@Test
	@Description("isEmailRegistered: メールアドレスが未登録ならfalseを返すこと")
	public void isEmailRegistered_test_2() {
		String email = "taro.samurai@example.com";

		when(userRepository.findByEmail(email)).thenReturn(null);

		assertFalse(userService.isEmailRegistered(email));

		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致すればtrueを返すこと")
	public void isSamePassword_test_1() {
		String password = "aaa";
		String passwordConfirmation = "aaa";
		assertTrue(userService.isSamePassword(password, passwordConfirmation));
	}

	@Test
	@Description("isSamePassword: パスワードとパスワード（確認用）の入力値が一致しなければfalseを返すこと")
	public void isSamePassword_test_2() {
		String password = "aaa";
		String passwordConfirmation = "bbb";
		assertFalse(userService.isSamePassword(password, passwordConfirmation));
	}

	@Test
	@Description("enableUser: ユーザーを有効にする")
	public void enableUser_test_1() {

		User user = new User();
		userService.enableUser(user);

		assertTrue(user.getEnabled());

		User expectedUser = new User();
		expectedUser.setEnabled(true);
		verify(userRepository, times(1)).save(expectedUser);
	}

	@Test
	@Description("enablePassword: 再設定パスワードを有効にする")
	public void enablePassword_test_1() {

		PasswordResetForm passwordResetForm = new PasswordResetForm();

		when(passwordEncoder.encode(passwordResetForm.getPassword()))
				.thenReturn("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		userService.enablePassword(passwordResetForm, new User());

		User expectedUser = new User();

		expectedUser.setPassword("$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO");

		verify(userRepository, times(1)).save(expectedUser);
	}

	@Test
	@Description("findAllUsers: すべてのユーザーをページングされた状態で取得する")
	public void findAllUsers_test_1() {
		List<User> users = List.of(new User(), new User());
		Page<User> pageResult = new PageImpl<>(users, PageRequest.of(0, 15), users.size());

		when(userRepository.findAll(any(Pageable.class)))
				.thenReturn(pageResult);
		Pageable pageable = PageRequest.of(0, 15);
		Page<User> page = userService.findAllUsers(pageable);

		Pageable expectedPageable = PageRequest.of(0, 15);
		verify(userRepository, times(1)).findAll(expectedPageable);

		Page<User> expectedPage = userService.findAllUsers(expectedPageable);

		assertEquals(page, expectedPage);

	}

	@Test
	@Description("findUsersByNameLikeOrFuriganaLike: 指定されたキーワードを氏名またはフリガナに含むユーザーを、ページングされた状態で取得すること")
	public void findUsersByNameLikeOrFuriganaLike_test_1() {

		List<User> users = List.of(new User(), new User());
		Page<User> pageResult = new PageImpl<>(users, PageRequest.of(0, 15), users.size());

		when(userRepository.findByNameLikeOrFuriganaLike(any(String.class), any(String.class), any(Pageable.class)))
				.thenReturn(pageResult);
		Pageable pageable = PageRequest.of(0, 15);

		String nameKeyword = "名前";
		String furiganaKeyword = "フリガナ";

		Page<User> page = userService.findUsersByNameLikeOrFuriganaLike(nameKeyword, furiganaKeyword, pageable);

		Pageable expectedPageable = PageRequest.of(0, 15);
		String expectedNameKeyword = "%名前%";
		String expectedFuriganaKeyword = "%フリガナ%";

		verify(userRepository, times(1)).findByNameLikeOrFuriganaLike(expectedNameKeyword, expectedFuriganaKeyword,
				expectedPageable);

		Page<User> expectedPage = userService.findUsersByNameLikeOrFuriganaLike(expectedNameKeyword,
				expectedFuriganaKeyword, expectedPageable);

		assertEquals(expectedPage, page);

	}

	@Test
	@Description("findUserById: 指定したidを持つユーザーを取得すること")
	public void findUserById_test_1() {

		when(userRepository.findById(anyInt())).thenReturn(Optional.of(new User()));

		Integer id = 1;
		Optional<User> user = userService.findUserById(id);

		Integer expectedId = 1;
		verify(userRepository, times(1)).findById(expectedId);

		Optional<User> expectedUser = userService.findUserById(expectedId);

		assertEquals(expectedUser, user);
	}

	@Test
	@Description("countUsersByRole_Name: 指定したロール名に紐づくユーザーのレコード数を取得すること")
	public void countUsersByRole_Name_test_1() {

		when(userRepository.countByRole_Name(anyString())).thenReturn(5L);

		String roleName = "ROLE_FREE_MEMBER";
		long roleCount = userService.countUsersByRole_Name(roleName);

		String expectedRoleName = "ROLE_FREE_MEMBER";

		verify(userRepository, times(1)).countByRole_Name(expectedRoleName);

		long expectedRoleCount = 5L;
		assertEquals(expectedRoleCount, roleCount);
	}

	@Test
	@Description("isEmailChanged: メールアドレスが変更されていればtrueを返すこと")
	public void isEmailChanged_test_1() {
		UserEditForm userEditForm = new UserEditForm();
		User user = new User();

		userEditForm.setEmail("taro.samurai@example.com");
		user.setEmail("jiro.samurai@example.com");

		assertTrue(userService.isEmailChanged(userEditForm, user));
	}

	@Test
	@Description("isEmailChanged: メールアドレスが変更されていなければfalseを返すこと")
	public void isEmailChanged_test_2() {
		UserEditForm userEditForm = new UserEditForm();
		User user = new User();

		userEditForm.setEmail("taro.samurai@example.com");
		user.setEmail("taro.samurai@example.com");

		assertFalse(userService.isEmailChanged(userEditForm, user));
	}

	@Test
	@Description("findUserByEmail: 指定したメールアドレスを持つユーザーを取得すること")
	public void findUserByEmail_test_1() {

		String email = "taro.samurai@example.com";
		User user = new User();
		when(userRepository.findByEmail(email)).thenReturn(user);

		User user2 = userService.findUserByEmail(email);

		String expectedEmail = "taro.samurai@example.com";

		verify(userRepository, times(1)).findByEmail(expectedEmail);
		User expectedUser = new User();
		assertEquals(expectedUser, user2);
	}

	@Test
	@Description("saveStripeCustomerId: saveメソッドが正しい引数で呼ばれていること")
	public void saveStripeCustomerId_test_1() {

		User user = new User();
		String stripeCustomerId = "1";

		userService.saveStripeCustomerId(user, stripeCustomerId);

		User expectedUser = new User();
		String expectedStripeCustomerId = "1";

		expectedUser.setStripeCustomerId(expectedStripeCustomerId);
		verify(userRepository, times(1)).save(expectedUser);

		assertEquals(expectedUser, user);
	}

	@Test
	@Description("updateRole: saveメソッドが正しい引数で呼ばれていること")
	public void updateRole_test_1() {

		User user = new User();
		String roleName = "ROLE_FREE_MEMBER";

		when(roleRepository.findByName(roleName)).thenReturn(new Role());
		userService.updateRole(user, roleName);

		verify(roleRepository, times(1)).findByName(roleName);

		User expectedUser = new User();
		String expectedRoleName = "ROLE_FREE_MEMBER";

		expectedUser.setRole(roleRepository.findByName(expectedRoleName));

		verify(userRepository, times(1)).save(expectedUser);

		assertEquals(expectedUser, user);

	}

	@Test
	@Description("findActiveByEmail: 指定したメールアドレスを持つ未退会ユーザーを取得すること")
	public void findActiveByEmail_test_1() {

		String email = "taro.samurai@example.com";
		when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.ofNullable(new User()));

		userService.findActiveByEmail(email);

		String expectedEmail = "taro.samurai@example.com";

		verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(expectedEmail);

		assertEquals(expectedEmail, email);
	}

	@Test
	@Description("withdrawal: saveメソッドが正しい引数で呼ばれていること")
	public void withdrawal_test_1() {

		String reason = "それっぽい値（具体的な退会理由）";

		// userに対してwithdrawalを適用する
		userService.withdrawal(new UserWithdrawalForm(), new User(), reason);

		User expectedUser = new User();

		// expectedUserに対してLocalDateTime.now()を設定
		expectedUser.setDeletedAt(LocalDateTime.now());

		// userと同じように、expectedUserの各種フィールドをsetする
		expectedUser.setDeletedByUser(true);
		expectedUser.setEnabled(false);
		expectedUser.setDeleteReason("それっぽい値（具体的な退会理由）");

		verify(userRepository, times(1)).save(expectedUser);
	}

	@Test
	@Description("rejoinByEmail: メールアドレスが登録されており、かつ、ユーザーが有効でない場合に、saveメソッドが正しい引数で呼ばれていること")
	public void rejoinByEmail_test_1() {

		String email = "taro.samurai@example.com";
		User user = new User();

		when(userRepository.findByEmail(email)).thenReturn(user);

		userService.rejoinByEmail(email);

		User expectedUser = new User();

		// userと同じように、expectedUserの各種フィールドをsetする
		expectedUser.setDeletedAt(null);
		expectedUser.setDeletedByUser(null);
		expectedUser.setDeleteReason(null);

		expectedUser.setEnabled(true);
		verify(userRepository, times(1)).save(expectedUser);

		assertEquals(expectedUser, user);
	}

	@Test
	@Description("rejoinByEmail: メールアドレスが登録されていない場合、例外処理が実行できること")
	public void rejoinByEmail_test_2() {

		User user = new User();
		String email = "taro.samurai@example.com";

		when(userRepository.findByEmail(email)).thenReturn(null);

		assertThrows(RejoinUserNotFoundException.class, () -> {
			userService.rejoinByEmail(email);
		});

		verify(userRepository, times(0)).save(user);
	}

	@Test
	@Description("rejoinByEmail: ユーザーが既に有効な場合、例外処理が実行できること")
	public void rejoinByEmail_test_3() {

		String email = "taro.samurai@example.com";

		User user = new User();
		user.setEnabled(true);

		when(userRepository.findByEmail(email)).thenReturn(user);

		assertThrows(AlreadyEnabledException.class, () -> {
			userService.rejoinByEmail(email);
		});

	}

	@Test
	@Description("refreshAuthenticationByRole: 認証情報のロールを更新できること")
	public void refreshAuthenticationByRole_test_1() {
		String newRole = "ROLE_FREE_MEMBER";
		userService.refreshAuthenticationByRole(newRole);

		String expectedRole = "ROLE_FREE_MEMBER";
		String actualRole = SecurityContextHolder.getContext().getAuthentication()
				.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.findFirst()
				.orElse(null);
		assertEquals(expectedRole, actualRole);

	}
}
