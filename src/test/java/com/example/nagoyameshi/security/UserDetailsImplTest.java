package com.example.nagoyameshi.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;

public class UserDetailsImplTest {

	private UserDetailsImpl userDetailsImpl;

	User expectedActiveUser = new User();

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

		this.userDetailsImpl = new UserDetailsImpl(expectedActiveUser,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

	}

	@Test
	@Description("getUser：User型のインスタンスを返すこと")
	public void getUser_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		User user = userDetailsImpl.getUser();

		// 3 then（期待値）
		assertEquals(expectedActiveUser, user);
	}

	@Test
	@Description("getPassword：登録されたパスワードを返すこと")
	public void getPassword_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		String password = userDetailsImpl.getPassword();

		// 3 then（期待値）
		assertEquals("password", password);
	}

	@Test
	@Description("getUsername：ログイン時に利用するユーザー名（メールアドレス）を返すこと")
	public void getUsername_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		String userName = userDetailsImpl.getUsername();

		// 3 then（期待値）
		assertEquals("taro.samurai@example.com", userName);
	}

	@Test
	@Description("getAuthorities：ロールのコレクションを返すこと")
	public void getAuthorities_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		Collection<? extends GrantedAuthority> authorities = userDetailsImpl.getAuthorities();

		// 3 then（期待値）
		assertEquals(List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")), authorities);
	}

	@Test
	@Description("isAccountNonExpired：アカウントが期限切れでなければtrueを返すこと")
	public void isAccountNonExpired_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		Boolean hasAccountNonExpired = userDetailsImpl.isAccountNonExpired();

		// 3 then（期待値）
		assertTrue(hasAccountNonExpired);
	}

	@Test
	@Description("isAccountNonLocked：ユーザーがロックされていなければtrueを返すこと")
	public void isAccountNonLocked_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		Boolean hasAccountNonLocked = userDetailsImpl.isAccountNonLocked();

		// 3 then（期待値）
		assertTrue(hasAccountNonLocked);
	}

	@Test
	@Description("isCredentialsNonExpired：ユーザーのパスワードが期限切れでなければtrueを返すこと")
	public void isCredentialsNonExpired_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		Boolean hasCredentialsNonExpired = userDetailsImpl.isCredentialsNonExpired();

		// 3 then（期待値）
		assertTrue(hasCredentialsNonExpired);
	}

	@Test
	@Description("isEnabled：ユーザーが有効であればtrueを返すこと")
	public void isEnabled_test1() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		Boolean hasEnabled = userDetailsImpl.isEnabled();

		// 3 then（期待値）
		assertTrue(hasEnabled);
	}

	@Test
	@Description("isEnabled：無効なユーザーならfalseを返すこと")
	public void isEnabled_test2() {
		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		expectedActiveUser.setEnabled(false);

		// 3 then（期待値）

		assertFalse(userDetailsImpl.isEnabled());
	}

}
