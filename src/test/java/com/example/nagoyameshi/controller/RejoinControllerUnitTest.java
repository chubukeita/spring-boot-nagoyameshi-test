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

import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.service.RejoinService;
import com.example.nagoyameshi.service.RejoinTokenService;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.errorMessage.RejoinResult;

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
  @Description("GET /rejoin/verify 無効トークンならverify画面にエラーを表示する")
  public void verify_test_1() throws Exception {
    when(rejoinService.rejoin("invalid"))
        .thenReturn(RejoinResult.error(
            2,
            "トークンが無効です。",
            "恐れ入りますが、一度開いたURLを再度開くことはできないので、再度メール認証からやり直してください。",
            "再入会手続きへ",
            "/rejoin"));

    mockMvc.perform(get("/rejoin/verify").param("token", "invalid"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/verify"))
        .andExpect(model().attribute("errorId", 2))
        .andExpect(model().attribute("errorMessage",
            "トークンが無効です。"))
        .andExpect(model().attribute("nextActionMessage",
            "恐れ入りますが、一度開いたURLを再度開くことはできないので、再度メール認証からやり直してください。"))
        .andExpect(model().attribute("buttonText", "再入会手続きへ"))
        .andExpect(model().attribute("buttonUrl", "/rejoin"));

    verify(rejoinService).rejoin("invalid");
  }

  @WithMockUser
  @Test
  @Description("GET /rejoin/verify 有効トークンなら再入会処理を行い確認画面を表示する")
  public void verify_test_2() throws Exception {
    when(rejoinService.rejoin("valid"))
        .thenReturn(RejoinResult.success(
            "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。",
            "ログインページへ",
            "/login"));

    mockMvc.perform(get("/rejoin/verify").param("token", "valid"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/verify"))
        .andExpect(model().attribute("successMessage",
            "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。"))
        .andExpect(model().attribute("buttonText", "ログインページへ"))
        .andExpect(model().attribute("buttonUrl", "/login"));

    verify(rejoinService).rejoin("valid");
  }

  @WithMockUser
  @Test
  @Description("GET /rejoin/verify 既に入会済みの場合はverify画面にID1エラーを表示する")
  public void verify_test_3() throws Exception {
    when(rejoinService.rejoin("alreadyEnabled"))
        .thenReturn(RejoinResult.error(
            1,
            "既にご利用中のアカウントです。",
            "現在入会中です。以下のボタンより、ホーム画面に戻って引き続き本サービスをご利用いただけます。",
            "ホームへ戻る",
            "/"));

    mockMvc.perform(get("/rejoin/verify").param("token", "alreadyEnabled"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/verify"))
        .andExpect(model().attribute("errorId", 1))
        .andExpect(model().attribute("errorMessage", "既にご利用中のアカウントです。"))
        .andExpect(model().attribute("nextActionMessage",
            "現在入会中です。以下のボタンより、ホーム画面に戻って引き続き本サービスをご利用いただけます。"))
        .andExpect(model().attribute("buttonText", "ホームへ戻る"))
        .andExpect(model().attribute("buttonUrl", "/"));

    verify(rejoinService).rejoin("alreadyEnabled");
  }

  @WithMockUser
  @Test
  @Description("GET /rejoin/verify ユーザー未存在の場合はverify画面にID3エラーを表示する")
  public void verify_test_4() throws Exception {
    when(rejoinService.rejoin("missingUser"))
        .thenReturn(RejoinResult.error(
            3,
            "該当メールアドレスのユーザーが見つかりません。",
            "恐れ入りますが、登録したメールアドレスをご確認のうえ、再度メール認証からやり直してください。",
            "再入会手続きへ",
            "/rejoin"));

    mockMvc.perform(get("/rejoin/verify").param("token", "missingUser"))
        .andExpect(status().isOk())
        .andExpect(view().name("rejoin/verify"))
        .andExpect(model().attribute("errorId", 3))
        .andExpect(model().attribute("errorMessage", "該当メールアドレスのユーザーが見つかりません。"))
        .andExpect(model().attribute("nextActionMessage",
            "恐れ入りますが、登録したメールアドレスをご確認のうえ、再度メール認証からやり直してください。"))
        .andExpect(model().attribute("buttonText", "再入会手続きへ"))
        .andExpect(model().attribute("buttonUrl", "/rejoin"));

    verify(rejoinService).rejoin("missingUser");
  }
}
