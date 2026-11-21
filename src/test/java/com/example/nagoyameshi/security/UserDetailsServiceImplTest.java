package com.example.nagoyameshi.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	UserDetailsServiceImpl userDetailsServiceImpl;

	@Test
	@Description("loadUserByUsernameのテスト：該当のユーザーが見つかったら、UserDetailsImplを返すこと。")
	public void loadUserByUsername_test1() throws Exception {

		String expectedEmail = "taro.samurai@example.com";
		User expectedUser = new User();
		Role expectedRole = new Role();
		String expectedRoleName = "TESTROLE";

		expectedUser.setRole(expectedRole);
		expectedRole.setName(expectedRoleName);

		Collection<GrantedAuthority> expectedAuthorities = new ArrayList<>();

		expectedAuthorities.add(new SimpleGrantedAuthority(expectedRoleName));

		UserDetails expectedDetailsImpl = new UserDetailsImpl(expectedUser, expectedAuthorities);

		// 1 given（どういう場合、設定を書く）
		when(userRepository.findByEmail(expectedEmail)).thenReturn(expectedUser);

		// 2 when（実行）
		UserDetails userDetailsImpl = userDetailsServiceImpl.loadUserByUsername(expectedEmail);

		// 3 then（期待値）
		verify(userRepository, times(1)).findByEmail(expectedEmail);

		assertThat(userDetailsImpl).usingRecursiveComparison().isEqualTo(expectedDetailsImpl);
	}

	@Test
	@Description("loadUserByUsernameのテスト：該当のユーザーが見つからなかったら、UsernameNotFoundExceptionの例外をthrowすること。")
	public void loadUserByUsername_test2() throws Exception {

		String expectedEmail = "taro.samurai@example.com";

		// 1 given（どういう場合、設定を書く）
		when(userRepository.findByEmail(expectedEmail)).thenReturn(null);

		// 2 when（実行）

		// 3 then（期待値）
		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
			userDetailsServiceImpl.loadUserByUsername(expectedEmail);
		});

		assertEquals("ユーザーが見つかりませんでした。", exception.getMessage());

		verify(userRepository, times(1)).findByEmail(expectedEmail);

	}
}
