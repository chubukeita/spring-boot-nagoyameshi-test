package com.example.nagoyameshi.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.CompanyService;
import com.example.nagoyameshi.service.ResetService;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.TermService;

@SpringBootTest // WebSecurityConfigTestは単体テストの予定ではあるが、必要に応じて@SpringBootTestを使って結合テスト寄りの単体テストとして動かす。
@AutoConfigureMockMvc
class WebSecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserDetailsServiceImpl userDetailsServiceImpl;

	@MockBean
	private CategoryService categoryService;

	@MockBean
	private TermService termService;

	@MockBean
	private CompanyService companyService;

	@MockBean
	private RestaurantService restaurantService;

	@MockBean
	private ResetService resetService;

	private UserDetailsImpl userprincipalFreeMember;
	private UserDetailsImpl userprincipalPaidMember;
	private UserDetailsImpl userprincipalAdmin;

	private Category washokuCategory;
	private Category udonCategory;
	private Category donCategory;
	private Category ramenCategory;
	private Category odenCategory;
	private Category friedCategory;

	private Term term;
	private Company company;

	private Restaurant restaurant;

	@BeforeEach
	public void setUp() {
		// 無料会員
		User expectedFreeMemberUser = new User();

		String expectedFreeMemberRoleName = "ROLE_FREE_MEMBER";
		Role expectedFreeMemberRole = new Role();
		expectedFreeMemberRole.setName(expectedFreeMemberRoleName);

		expectedFreeMemberUser.setName("侍 太郎");
		expectedFreeMemberUser.setFurigana("サムライ タロウ");
		expectedFreeMemberUser.setPostalCode("1010022");
		expectedFreeMemberUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedFreeMemberUser.setPhoneNumber("09012345678");
		expectedFreeMemberUser.setBirthday(LocalDate.of(1990, 1, 1));
		expectedFreeMemberUser.setOccupation("エンジニア");
		expectedFreeMemberUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedFreeMemberUser.setRole(expectedFreeMemberRole);
		expectedFreeMemberUser.setEmail("taro.samurai@example.com");
		expectedFreeMemberUser.setEnabled(true);

		this.userprincipalFreeMember = new UserDetailsImpl(expectedFreeMemberUser,
				List.of(new SimpleGrantedAuthority(expectedFreeMemberRoleName)));

		// 有料会員
		User expectedPaidMemberUser = new User();

		String expectedPaidMemberRoleName = "ROLE_PAID_MEMBER";
		Role expectedPaidMemberRole = new Role();
		expectedPaidMemberRole.setName(expectedPaidMemberRoleName);

		expectedPaidMemberUser.setName("侍 次郎");
		expectedPaidMemberUser.setFurigana("サムライ ジロウ");
		expectedPaidMemberUser.setPostalCode("1010022");
		expectedPaidMemberUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedPaidMemberUser.setPhoneNumber("09012345679");
		expectedPaidMemberUser.setBirthday(LocalDate.of(1991, 2, 2));
		expectedPaidMemberUser.setOccupation("デザイナー");
		expectedPaidMemberUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedPaidMemberUser.setRole(expectedPaidMemberRole);
		expectedPaidMemberUser.setEmail("jiro.samurai@example.com");
		expectedPaidMemberUser.setEnabled(true);

		this.userprincipalPaidMember = new UserDetailsImpl(expectedPaidMemberUser,
				List.of(new SimpleGrantedAuthority(expectedPaidMemberRoleName)));

		// 管理者
		User expectedAdminUser = new User();

		String expectedAdminRoleName = "ROLE_ADMIN";
		Role expectedAdminRole = new Role();
		expectedAdminRole.setName(expectedAdminRoleName);

		expectedAdminUser.setName("侍 花子");
		expectedAdminUser.setFurigana("サムライ ハナコ");
		expectedAdminUser.setPostalCode("1010022");
		expectedAdminUser.setAddress("東京都千代田区神田練塀町300番地");
		expectedAdminUser.setPhoneNumber("09012345670");
		expectedAdminUser.setBirthday(LocalDate.of(1992, 3, 3));
		expectedAdminUser.setOccupation("マーケティング");
		expectedAdminUser.setPassword(new BCryptPasswordEncoder().encode("password"));
		expectedAdminUser.setRole(expectedAdminRole);
		expectedAdminUser.setEmail("hanako.samurai@example.com");
		expectedAdminUser.setEnabled(true);

		this.userprincipalAdmin = new UserDetailsImpl(expectedAdminUser,
				List.of(new SimpleGrantedAuthority(expectedAdminRoleName)));

		// "/"のエンドポイントで呼び出されるHomeController上のCategoryServiceをスタブ化するために初期設定を行う
		washokuCategory = new Category();
		washokuCategory.setName("和食");
		udonCategory = new Category();
		udonCategory.setName("うどん");
		donCategory = new Category();
		donCategory.setName("丼物");
		ramenCategory = new Category();
		ramenCategory.setName("ラーメン");
		odenCategory = new Category();
		odenCategory.setName("おでん");
		friedCategory = new Category();
		friedCategory.setName("揚げ物");

		term = new Term();
		term.setContent("利用規約");

		company = new Company();
		company.setName("会社概要");

		restaurant = new Restaurant();
		restaurant.setName("レストラン");

	}

	@Nested
	@DisplayName("ログイン・ログアウトに関するテスト")
	class securityFilterChain_LoginLogoutTest {
		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移すること")
		public void securityFilterChain_test1() throws Exception {
			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/login"))
					.andExpect(status().isOk());
			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/testへアクセスする場合に、ログインページへリダイレクトすること")
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
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移し、メールアドレスとパスワードを入力してログインボタンを押した際に、/?loggedInへリダイレクトされて、ログインが成功すること")
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

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移し、誤ったメールアドレスと、正しいパスワードを入力してログインボタンを押した際に、/login?errorへリダイレクトされて、ログインが失敗すること")
		public void securityFilterChain_test4() throws Exception {

			String expectedEmail = "wrong@example.com";
			String expectedPassword = "password";

			// 1 given（どういう場合、設定を書く）
			when(userDetailsServiceImpl.loadUserByUsername(expectedEmail))
					.thenThrow(new UsernameNotFoundException("ユーザーが見つかりませんでした。"));

			// 2 when（実行）
			mockMvc.perform(post("/login")
					.param("username", expectedEmail)
					.param("password", expectedPassword)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?error"));

			// 3 then（期待値）
			verify(userDetailsServiceImpl, times(1)).loadUserByUsername(expectedEmail);
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移し、正しいメールアドレスと、誤ったパスワードを入力してログインボタンを押した際に、/login?errorへリダイレクトされて、ログインが失敗すること")
		public void securityFilterChain_test5() throws Exception {

			String expectedEmail = "taro.samurai@example.com";
			String expectedPassword = "wrongpassword";

			// 1 given（どういう場合、設定を書く）
			when(userDetailsServiceImpl.loadUserByUsername(expectedEmail)).thenReturn(userprincipalFreeMember);

			// 2 when（実行）
			mockMvc.perform(post("/login")
					.param("username", expectedEmail)
					.param("password", expectedPassword)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?error"));

			// 3 then（期待値）
			verify(userDetailsServiceImpl, times(1)).loadUserByUsername(expectedEmail);
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが/loginへアクセスする場合に、ログインページへ遷移し、誤ったメールアドレスと、誤ったパスワードを入力してログインボタンを押した際に、/login?errorへリダイレクトされて、ログインが失敗すること")
		public void securityFilterChain_test6() throws Exception {

			String expectedEmail = "wrong@example.com";
			String expectedPassword = "wrongpassword";

			// 1 given（どういう場合、設定を書く）
			when(userDetailsServiceImpl.loadUserByUsername(expectedEmail))
					.thenThrow(new UsernameNotFoundException("ユーザーが見つかりませんでした。"));

			// 2 when（実行）
			mockMvc.perform(post("/login")
					.param("username", expectedEmail)
					.param("password", expectedPassword)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?error"));

			// 3 then（期待値）
			verify(userDetailsServiceImpl, times(1)).loadUserByUsername(expectedEmail);
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：ログインユーザーがログアウトボタンを押下した時、/?loggedOutへリダイレクトされて、ログアウトが成功すること")
		public void securityFilterChain_test7() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post("/logout")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/?loggedOut"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：ログインユーザーがログアウトボタンを押下した時、/?loggedOutへリダイレクトされて、ログアウトが成功すること")
		public void securityFilterChain_test8() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post("/logout")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/?loggedOut"));

			// 3 then（期待値）
		}

	}

	@Nested
	@DisplayName("未ログインユーザーのアクセス権限のテスト")
	class securityFilterChain_ANONYMOUS {

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@CsvSource({
				"/css/style.css,text/css",
				"/images/logo.svg,image/svg+xml",
				"/js/carousel.js,text/javascript",
				"/storage/test.jpg,image/jpeg"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/css/**, /images/**, /js/**, /storage/**,ページへアクセスできること")
		public void securityFilterChain_test9(String url, String expectedContentType) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(expectedContentType))
					.andExpect(unauthenticated());

			// 3 then（期待値）

		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/ページへアクセスできること")
		public void securityFilterChain_test10() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(categoryService.findFirstCategoryByName("和食")).thenReturn(washokuCategory);
			when(categoryService.findFirstCategoryByName("うどん")).thenReturn(udonCategory);
			when(categoryService.findFirstCategoryByName("丼物")).thenReturn(donCategory);
			when(categoryService.findFirstCategoryByName("ラーメン")).thenReturn(ramenCategory);
			when(categoryService.findFirstCategoryByName("おでん")).thenReturn(odenCategory);
			when(categoryService.findFirstCategoryByName("揚げ物")).thenReturn(friedCategory);

			// 2 when（実行）
			mockMvc.perform(get("/")
					.with(csrf()))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(categoryService, times(1)).findFirstCategoryByName("和食");
			verify(categoryService, times(1)).findFirstCategoryByName("うどん");
			verify(categoryService, times(1)).findFirstCategoryByName("丼物");
			verify(categoryService, times(1)).findFirstCategoryByName("ラーメン");
			verify(categoryService, times(1)).findFirstCategoryByName("おでん");
			verify(categoryService, times(1)).findFirstCategoryByName("揚げ物");

			// （備考）もし結合テストに回すとしたら以下のテストコードとして検証することになる。
			//		@SpringBootTest
			//		@AutoConfigureMockMvc
			//		@ActiveProfiles("test")
			//		class WebSecurityConfigIntegrationTest {
			//
			//		    @Autowired
			//		    private MockMvc mockMvc;
			//
			//		    @Test
			//		    @Sql(
			//		        statements = {
			//		            "INSERT INTO categories (id, name) VALUES (1001, '和食')",
			//		            "INSERT INTO categories (id, name) VALUES (1002, 'うどん')",
			//		            "INSERT INTO categories (id, name) VALUES (1003, '丼物')",
			//		            "INSERT INTO categories (id, name) VALUES (1004, 'ラーメン')",
			//		            "INSERT INTO categories (id, name) VALUES (1005, 'おでん')",
			//		            "INSERT INTO categories (id, name) VALUES (1006, '揚げ物')"
			//		        },
			//		        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
			//		    )
			//		    @Sql(
			//		        statements = {
			//		            "DELETE FROM categories WHERE id BETWEEN 1001 AND 1006"
			//		        },
			//		        executionPhase = ExecutionPhase.AFTER_TEST_METHOD
			//		    )
			//		    void root_is_permitAll_for_anonymous() throws Exception {
			//		        mockMvc.perform(get("/"))
			//		               .andExpect(status().isOk())                // ログイン画面へ飛ばされない
			//		               .andExpect(view().name("index"))           // index表示
			//		               .andExpect(unauthenticated());             // 匿名のまま
			//		    }
			//
			//		    @Test
			//		    void protected_url_redirects_to_login_for_anonymous() throws Exception {
			//		        mockMvc.perform(get("/test"))
			//		               .andExpect(status().is3xxRedirection())
			//		               .andExpect(redirectedUrl("http://localhost/login"))
			//		               .andExpect(unauthenticated());
			//		    }
			//		}
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/signup",
				"/rejoin",
				"/resetPassword",
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/signup、/rejoin、/resetPasswordページへアクセスできること")
		public void securityFilterChain_test11(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf()))
					.andExpect(status().isOk());

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/resetPasswordVerificationページへアクセスできること")
		public void securityFilterChain_test12() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(resetService.isValidToken("testtoken")).thenReturn(true);

			// 2 when（実行）
			mockMvc.perform(get("/resetPasswordVerification")
					.param("token", "testtoken")
					.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(view().name("auth/resetPasswordVerification"))
					.andExpect(model().attribute("token", "testtoken"));

			// 3 then（期待値）
			verify(resetService, times(1)).isValidToken("testtoken");
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/restaurants/1/reviews",
				"/reservations",
				"/restaurants/1/reservations",
				"/favorites",
				"/restaurants/1/favorites",
				"/withdrawal"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/restaurants/{restaurantId}/reviews、/reservations、/restaurants/{restaurantId}/reservations、/favorites、/restaurants/{restaurantId}/favorites、/withdrawalページにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test13(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/restaurantsページへアクセスできること")
		public void securityFilterChain_test14() throws Exception {

			// 1 given（どういう場合、設定を書く）
			PageRequest pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
			Page<Restaurant> emptyPage = new PageImpl<>(List.of());
			when(restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable))
					.thenReturn(emptyPage);
			when(categoryService.findAllCategories()).thenReturn(List.of());

			// 2 when（実行）
			mockMvc.perform(get("/restaurants")
					.with(csrf()))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(restaurantService, times(1)).findAllRestaurantsByOrderByCreatedAtDesc(pageable);
			verify(categoryService, times(1)).findAllCategories();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/companyページへアクセスできること")
		public void securityFilterChain_test15() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

			// 2 when（実行）
			mockMvc.perform(get("/company")
					.with(csrf()))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(companyService, times(1)).findFirstCompanyByOrderByIdDesc();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/termsページへアクセスできること")
		public void securityFilterChain_test16() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

			// 2 when（実行）
			mockMvc.perform(get("/terms")
					.with(csrf()))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(termService, times(1)).findFirstTermByOrderByIdDesc();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、POST /subscription/createにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test17() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post("/subscription/create")
					.param("paymentMethodId", "pm_test")
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"))
					.andExpect(result -> assertEquals("pm_test", result.getRequest().getParameter("paymentMethodId")));

			// 3 then（期待値）
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/subscription/register",
				"/subscription/create"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、GET /subscription/register、GET /subscription/create にアクセスされずにリダイレクトされること")
		public void securityFilterChain_test18(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"));

			// 3 then（期待値）
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/subscription/edit",
				"/subscription/cancel"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/subscription/edit、/subscription/cancelページにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test19(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"));

			// 3 then（期待値）
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/subscription/update",
				"/subscription/delete"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、POST /subscription/update、POST /subscription/deleteにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test20(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post(url)
					.param("paymentMethodId", "pm_test")
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"))
					.andExpect(result -> assertEquals("pm_test", result.getRequest().getParameter("paymentMethodId")));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/adminページにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test21() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/admin")
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：未ログインユーザーが認証していない状態で、/userページにアクセスされずにリダイレクトされること")
		public void securityFilterChain_test22() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/user")
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("http://localhost/login"));

			// 3 then（期待値）
		}
	}

	@Nested
	@DisplayName("無料会員のアクセス権限のテスト")
	class securityFilterChain_FREE_MEMBER {

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@CsvSource({
				"/css/style.css,text/css",
				"/images/logo.svg,image/svg+xml",
				"/js/carousel.js,text/javascript",
				"/storage/test.jpg,image/jpeg"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/css/**, /images/**, /js/**, /storage/**,ページへアクセスできること")
		public void securityFilterChain_test23(String url, String expectedContentType) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith(expectedContentType));

			// 3 then（期待値）

		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/ページへアクセスできること")
		public void securityFilterChain_test24() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(categoryService.findFirstCategoryByName("和食")).thenReturn(washokuCategory);
			when(categoryService.findFirstCategoryByName("うどん")).thenReturn(udonCategory);
			when(categoryService.findFirstCategoryByName("丼物")).thenReturn(donCategory);
			when(categoryService.findFirstCategoryByName("ラーメン")).thenReturn(ramenCategory);
			when(categoryService.findFirstCategoryByName("おでん")).thenReturn(odenCategory);
			when(categoryService.findFirstCategoryByName("揚げ物")).thenReturn(friedCategory);

			// 2 when（実行）
			mockMvc.perform(get("/")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(categoryService, times(1)).findFirstCategoryByName("和食");
			verify(categoryService, times(1)).findFirstCategoryByName("うどん");
			verify(categoryService, times(1)).findFirstCategoryByName("丼物");
			verify(categoryService, times(1)).findFirstCategoryByName("ラーメン");
			verify(categoryService, times(1)).findFirstCategoryByName("おでん");
			verify(categoryService, times(1)).findFirstCategoryByName("揚げ物");
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/signup",
				"/rejoin",
				"/resetPassword",
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/signup、/rejoin、/resetPasswordページへアクセスできること")
		public void securityFilterChain_test25(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/resetPasswordVerificationページへアクセスできること")
		public void securityFilterChain_test26() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(resetService.isValidToken("testtoken")).thenReturn(true);

			// 2 when（実行）
			mockMvc.perform(get("/resetPasswordVerification")
					.param("token", "testtoken")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk())
					.andExpect(view().name("auth/resetPasswordVerification"))
					.andExpect(model().attribute("token", "testtoken"));

			// 3 then（期待値）
			verify(resetService, times(1)).isValidToken("testtoken");
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/reservations",
				"/favorites"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/restaurants/{restaurantId}/reviews、/reservations、/restaurants/{restaurantId}/reservations、/favorites、/restaurants/{restaurantId}/favorites、/withdrawalページへアクセスできること")
		public void securityFilterChain_test27(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/subscription/register"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、GET /restaurants/{restaurantId}/reviews/register にアクセスすると /subscription/register へリダイレクトされること")
		public void securityFilterChain_test28() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(restaurantService.findRestaurantById(1)).thenReturn(Optional.of(restaurant));

			// 2 when（実行）
			mockMvc.perform(get("/restaurants/1/reviews/register")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/subscription/register"));

			// 3 then（期待値）
			verify(restaurantService, never()).findRestaurantById(1);
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、GET /restaurants/{restaurantId}/reservations/register にアクセスすると /subscription/register へリダイレクトされること")
		public void securityFilterChain_test29() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/restaurants/1/reservations/register")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/subscription/register"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、GET /favorites にアクセスすると /subscription/register へリダイレクトされること")
		public void securityFilterChain_test30() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/favorites")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/subscription/register"));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、GET /withdrawal/delete にアクセスできること")
		public void securityFilterChain_test31() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/withdrawal/delete")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/restaurantsページへアクセスできること")
		public void securityFilterChain_test32() throws Exception {

			// 1 given（どういう場合、設定を書く）
			PageRequest pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
			Page<Restaurant> emptyPage = new PageImpl<>(List.of());
			when(restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable))
					.thenReturn(emptyPage);
			when(categoryService.findAllCategories()).thenReturn(List.of());

			// 2 when（実行）
			mockMvc.perform(get("/restaurants")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(restaurantService, times(1)).findAllRestaurantsByOrderByCreatedAtDesc(pageable);
			verify(categoryService, times(1)).findAllCategories();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/companyページへアクセスできること")
		public void securityFilterChain_test33() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

			// 2 when（実行）
			mockMvc.perform(get("/company")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(companyService, times(1)).findFirstCompanyByOrderByIdDesc();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/termsページへアクセスできること")
		public void securityFilterChain_test34() throws Exception {

			// 1 given（どういう場合、設定を書く）
			when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

			// 2 when（実行）
			mockMvc.perform(get("/terms")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
			verify(termService, times(1)).findFirstTermByOrderByIdDesc();
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、GET /subscription/register にアクセスできること")
		public void securityFilterChain_test35() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/subscription/register")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、POST /subscription/create にアクセスできること")
		public void securityFilterChain_test36() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post("/subscription/create")
					.param("paymentMethodId", "pm_test")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/"))
					.andExpect(result -> assertEquals("pm_test", result.getRequest().getParameter("paymentMethodId")));

			// 3 then（期待値）
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/subscription/edit",
				"/subscription/cancel"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/subscription/edit、/subscription/cancelページが表示されずに403エラーが発生すること")
		public void securityFilterChain_test37(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get(url)
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isForbidden());

			// 3 then（期待値）
		}

		@ParameterizedTest(name = "No{index} ; アクセス先: {arguments}")
		@ValueSource(strings = {
				"/subscription/update",
				"/subscription/delete"
		})
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、POST /subscription/update、POST /subscription/deleteが表示されずに403エラーが発生すること")
		public void securityFilterChain_test38(String url) throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(post(url)
					.param("paymentMethodId", "pm_test")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isForbidden())
					.andExpect(result -> assertEquals("pm_test", result.getRequest().getParameter("paymentMethodId")));

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/adminページが表示されずに403エラーが発生すること")
		public void securityFilterChain_test39() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/admin")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isForbidden());

			// 3 then（期待値）
		}

		@Test
		@DisplayName("securityFilterChain_SpringSecurityの認証、権限設定テスト：無料会員がログインしている状態で、/userページにアクセスできること")
		public void securityFilterChain_test40() throws Exception {

			// 1 given（どういう場合、設定を書く）

			// 2 when（実行）
			mockMvc.perform(get("/user")
					.with(csrf())
					.with(user(userprincipalFreeMember)))
					.andExpect(status().isOk());

			// 3 then（期待値）
		}
	}

	@Nested
	@DisplayName("passwordEncoderメソッドに関するテスト")
	class passwordEncoderTest {
		@Test
		@DisplayName("passwordEncoderのテスト：passwordEncoderがBCryptPasswordEncoderを返すこと")
		public void passwordEncoder_test1() throws Exception {

			// 1 given（どういう場合、設定を書く）
			WebSecurityConfig webSecurityConfig = new WebSecurityConfig();

			// 2 when（実行）
			PasswordEncoder passwordEncoder = webSecurityConfig.passwordEncoder();

			// 3 then（期待値）
			assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
		}

		@Test
		@DisplayName("passwordEncoderのテスト：passwordEncoderがパスワードをハッシュ化して照合できること")
		public void passwordEncoder_test2() throws Exception {

			// 1 given（どういう場合、設定を書く）
			WebSecurityConfig webSecurityConfig = new WebSecurityConfig();

			// 2 when（実行）
			PasswordEncoder passwordEncoder = webSecurityConfig.passwordEncoder();
			String encodedPassword = passwordEncoder.encode("password");

			// 3 then（期待値）
			assertNotEquals("password", encodedPassword);
			assertTrue(passwordEncoder.matches("password", encodedPassword));
			assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
		}
	}
}
