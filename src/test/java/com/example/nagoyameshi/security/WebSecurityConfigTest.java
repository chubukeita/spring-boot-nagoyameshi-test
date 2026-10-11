package com.example.nagoyameshi.security;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.User;

//実装
//@Bean
//public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//    http.authorizeHttpRequests(auth -> auth
//        .requestMatchers("/public/**").permitAll()
//        .anyRequest().authenticated()
//    );
//
//    return http.build();
//}

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserDetailsServiceImpl userDetailsServiceImpl;

	private UserDetailsImpl userprincipalFreeMember;
	private UserDetailsImpl userprincipalPaidMember;
	private UserDetailsImpl userprincipalAdmin;

	@BeforeEach
	public void setUp() {
		// 無料会員
		User expectedFreeMemberUser = new User();

		expectedFreeMemberUser.setName("侍 太郎");
		expectedFreeMemberUser.setFurigana("サムライ タロウ");
		expectedFreeMemberUser.setPostalCode("1010022");
		expectedFreeMemberUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedFreeMemberUser.setPhoneNumber("09012345678");
		expectedFreeMemberUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedFreeMemberUser.setOccupation("エンジニア");
		expectedFreeMemberUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedFreeMemberUser.setEmail("taro.samurai@example.com");
		expectedFreeMemberUser.setEnabled(true);

		this.userprincipalFreeMember = new UserDetailsImpl(expectedFreeMemberUser,
				List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

		// 有料会員
		User expectedPaidMemberUser = new User();

		expectedPaidMemberUser.setName("侍 次郎");
		expectedPaidMemberUser.setFurigana("サムライ ジロウ");
		expectedPaidMemberUser.setPostalCode("1010022");
		expectedPaidMemberUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedPaidMemberUser.setPhoneNumber("09012345679");
		expectedPaidMemberUser.setBirthday(LocalDate.of(1991, 2, 2));
		expectedPaidMemberUser.setOccupation("デザイナー");
		expectedPaidMemberUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedPaidMemberUser.setEmail("jiro.samurai@example.com");
		expectedPaidMemberUser.setEnabled(true);

		this.userprincipalPaidMember = new UserDetailsImpl(expectedPaidMemberUser,
				List.of(new SimpleGrantedAuthority("ROLE_PAID_MEMBER")));

		// 管理者
		User expectedAdminUser = new User();

		expectedAdminUser.setName("侍 花子");
		expectedAdminUser.setFurigana("サムライ ハナコ");
		expectedAdminUser.setPostalCode("1010022");
		expectedAdminUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedAdminUser.setPhoneNumber("09012345670");
		expectedAdminUser.setBirthday(LocalDate.of(1992, 3, 3));
		expectedAdminUser.setOccupation("マーケティング");
		expectedAdminUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedAdminUser.setEmail("hanako.samurai@example.com");
		expectedAdminUser.setEnabled(true);

		this.userprincipalAdmin = new UserDetailsImpl(expectedAdminUser,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	@Test
	@Description("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移すること")
	public void securityFilterChain_test1() throws Exception {
		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk());
		// 3 then（期待値）
	}

	@Test
	@Description("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/testへアクセスする場合に、ログインページへリダイレクトすること")
	public void securityFilterChain_test2() throws Exception {

		// 1 given（どういう場合、設定を書く）

		// 2 when（実行）
		mockMvc.perform(get("/test"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost" + "/login"));

		// localhostは自分自身のコンピュータを表している。
		// またテストコードは、自分自身でしか基本的には実行されないし、
		// GitHub Actionsなどで検証環境や本番環境で実行されるテストも基本的にはGit上で自分のlocalhost上で動いている。
		// もし相対パスで/loginとした場合、herokuや/google/loginのような自分のコンピュータではない、サーバーに対してアクセスを行うことになる。
		// なので、別の人が自分のアプリに直接アクセスした場合は、別のIPアドレスとしてlocalhostではない別のホスト名でアクセスされることになる

		// 3 then（期待値）

	}

	@Test
	@Description("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移し、メールアドレスとパスワードを入力してログインボタンを押した際に、/?loggedInへリダイレクトされて、ログインが成功すること")
	public void securityFilterChain_test3() throws Exception {

		String expectedEmail = "taro.samurai@example.com";
		String expectedPassword = "password";

		// 1 given（どういう場合、設定を書く）
		when(userDetailsServiceImpl.loadUserByUsername(expectedEmail)).thenReturn(userprincipalFreeMember);

		// 2 when（実行）
		mockMvc.perform(post("/login")
				.param("username", expectedEmail)
				.param("password", expectedPassword)
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/?loggedIn"));

		// 3 then（期待値）
		verify(userDetailsServiceImpl, times(1)).loadUserByUsername(expectedEmail);
	}

}
