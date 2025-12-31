package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserNavService;
import com.example.nagoyameshi.service.UserNavService.PreviewNext;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerUnitTest {

  @MockBean
  private UserService userService;

  @MockBean
  private UserNavService userNavService;

  @Autowired
  private MockMvc mockMvc;

  private UserDetailsImpl adminUserDetails;
  private User user;

  @BeforeEach
  public void setUp() {
    User adminUser = new User();
    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    adminUser.setRole(adminRole);
    adminUserDetails = new UserDetailsImpl(adminUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    user = new User();
    user.setId(1);
    user.setName("テストユーザー");
  }

  @Test
  @Description("GET /admin/users ログイン済みの場合はユーザー一覧ページが正しく表示される")
  public void index_test_1() throws Exception {
    Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
    Page<User> userPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);

    when(userService.findAllUsers(pageable)).thenReturn(userPage);

    mockMvc.perform(get("/admin/users").with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/users/index"))
        .andExpect(model().attribute("userPage", userPage));

    verify(userService).findAllUsers(pageable);
  }

  @Test
  @Description("GET /admin/users キーワード検索でユーザー一覧ページが正しく表示される")
  public void index_test_2() throws Exception {
    Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
    Page<User> userPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);
    String testKeyword = "テスト";

    when(userService.findUsersByNameLikeOrFuriganaLike(testKeyword, testKeyword, pageable))
        .thenReturn(userPage);

    mockMvc.perform(get("/admin/users")
        .param("keyword", testKeyword)
        .with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/users/index"))
        .andExpect(model().attribute("userPage", userPage))
        .andExpect(model().attribute("keyword", testKeyword));

    verify(userService).findUsersByNameLikeOrFuriganaLike(testKeyword, testKeyword, pageable);
  }

  @Test
  @Description("GET /admin/users/{id} ユーザーが存在しない場合はユーザー一覧ページにリダイレクトされる")
  public void show_test_1() throws Exception {
    Integer userId = 999;

    when(userService.findUserById(userId)).thenReturn(Optional.empty());

    mockMvc.perform(get("/admin/users/999").with(user(adminUserDetails)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/users"))
        .andExpect(flash().attribute("errorMessage", "ユーザーが存在しません。"));

    verify(userService).findUserById(userId);
  }

  @Test
  @Description("GET /admin/users/{id} ユーザーが存在する場合はユーザー詳細ページが正しく表示される")
  public void show_test_2() throws Exception {
    Integer userId = 1;
    PreviewNext previewNext = new PreviewNext(null, 2);

    when(userService.findUserById(userId)).thenReturn(Optional.of(user));
    when(userNavService.findNeighborsNameOnly(userId, null, null)).thenReturn(previewNext);
    when(userNavService.buildBackUrlForNameOnly(eq(userId), any(), eq(15), eq("/admin/users")))
        .thenReturn("/admin/users?page=0");

    mockMvc.perform(get("/admin/users/1").with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/users/show"))
        .andExpect(model().attribute("user", user));

    verify(userService).findUserById(userId);
    verify(userNavService).findNeighborsNameOnly(userId, null, null);
  }
}
