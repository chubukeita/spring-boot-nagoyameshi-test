package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.eq;
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

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;

@WebMvcTest(RejoinController.class)
public class RejoinControllerUnitTest {

  @MockBean
  private RejoinService rejoinService;

  @MockBean
  private RejoinTokenService rejoinTokenService;

  @MockBean
  private UserService userService;

  @Autowired
  private MockMvc mockMvc;

  private static final String BR_REJOIN_FORM = BindingResult.MODEL_KEY_PREFIX + "rejoinForm";

  @WithMockUser
  @Test
  @Description("GET /rejoin で再入会フォームを表示する")
  void showRejoinForm_test_1() throws Exception {
    mockMvc.perform(get("/rejoin"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/rejoin"))
        .andExpect(model().attribute("rejoinForm", new RejoinForm()));
  }

  @WithMockUser
  @Test
  @Description("POST /rejoin バリデーションエラー時は再入会フォームを再表示する")
  public void rejoin_test_1() throws Exception {
    mockMvc.perform(post("/rejoin").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/rejoin"))
        .andExpect(model().hasErrors())
        .andExpect(model().attributeHasFieldErrors("rejoinForm", "email"));
  }

  @WithMockUser
  @Test
  @Description("POST /rejoin 正常時は再入会メール送信してトップへリダイレクトする")
  public void rejoin_test_2() throws Exception {
    String email = "taro.samurai@example.com";

    mockMvc.perform(post("/rejoin")
        .with(csrf())
        .with(request -> {
          request.setScheme("http");
          request.setServerName("localhost");
          request.setServerPort(8080);
          return request;
        })
        .param("email", email))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(model().hasNoErrors())
        .andExpect(model().attributeDoesNotExist(BR_REJOIN_FORM))
        .andExpect(flash().attribute("successMessage",
            "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、再入会を完了してください。"));

    verify(rejoinService).requestRejoin(eq(email), eq("http://localhost:8080/rejoin"));
  }

  @WithMockUser
  @Test
  @Description("GET /rejoin/verify 無効トークンならエラーページを表示する")
  public void verify_test_1() throws Exception {
    when(rejoinTokenService.findRejoinTokenByToken("invalid"))
        .thenReturn(null);

    mockMvc.perform(get("/rejoin/verify").param("token", "invalid"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/invalid"))
        .andExpect(model().attribute("errorMessage",
            "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。"));

    verify(rejoinTokenService).findRejoinTokenByToken("invalid");
  }

  @WithMockUser
  @Test
  @Description("GET /rejoin/verify 有効トークンなら再入会処理を行い確認画面を表示する")
  public void verify_test_2() throws Exception {
    when(rejoinTokenService.findRejoinTokenByToken("valid"))
        .thenReturn(new RejoinToken());

    mockMvc.perform(get("/rejoin/verify").param("token", "valid"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/verify"))
        .andExpect(model().attribute("successMessage",
            "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。"));

    verify(rejoinTokenService).findRejoinTokenByToken("valid");
    verify(rejoinService).rejoin("valid");
  }
}
