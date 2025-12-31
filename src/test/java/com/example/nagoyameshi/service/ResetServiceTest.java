package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.nagoyameshi.entity.ResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.ResetEventPublisher;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.InvalidTokenException;
import com.example.nagoyameshi.service.error.PasswordMismatchException;
import com.example.nagoyameshi.service.error.UserNotFoundForTokenException;

@ExtendWith(MockitoExtension.class)
public class ResetServiceTest {

  @Mock
  private ResetTokenService resetTokenService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private ResetEventPublisher resetEventPublisher;

  @InjectMocks
  private ResetService resetService;

  @Test
  @Description("requestReset_メールとURLでイベントを発火すること")
  void requestReset_test_1() {
    resetService.requestReset("user@example.com", "http://example/reset");

    verify(resetEventPublisher).publishResetEvent("user@example.com", "http://example/reset");
  }

  @Test
  @Description("isValidToken_トークン存在でtrueを返すこと")
  void isValidToken_test_1() {
    when(resetTokenService.findResetTokenByToken("token")).thenReturn(new ResetToken());

    assertTrue(resetService.isValidToken("token"));
  }

  @Test
  @Description("isValidToken_トークンなしでfalseを返すこと")
  void isValidToken_test_2() {
    when(resetTokenService.findResetTokenByToken("token")).thenReturn(null);

    assertFalse(resetService.isValidToken("token"));
  }

  @Test
  @Description("resetPassword_パスワード不一致で例外")
  public void resetPassword_test_1() {
    PasswordResetForm form = new PasswordResetForm();
    form.setPassword("abc");
    form.setPasswordConfirmation("def");

    assertThrows(PasswordMismatchException.class, () -> resetService.resetPassword("token", form));

  }

  @Test
  @Description("resetPassword_トークン無効で例外")
  public void resetPassword_test_2() {
    PasswordResetForm form = new PasswordResetForm();
    form.setPassword("abc");
    form.setPasswordConfirmation("abc");
    when(resetTokenService.findResetTokenByToken("token")).thenReturn(null);

    assertThrows(InvalidTokenException.class, () -> resetService.resetPassword("token", form));
    verify(resetTokenService).findResetTokenByToken("token");
  }

  @Test
  @Description("resetPassword_ユーザー未存在で例外")
  public void resetPassword_test_3() {
    PasswordResetForm form = new PasswordResetForm();
    form.setPassword("abc");
    form.setPasswordConfirmation("abc");
    ResetToken rt = new ResetToken();
    rt.setEmail("user@example.com");
    when(resetTokenService.findResetTokenByToken("token")).thenReturn(rt);
    when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundForTokenException.class, () -> resetService.resetPassword("token", form));
    verify(userRepository, never()).save(new User());
  }

  @Test
  @Description("resetPassword_成功でパスワード更新とトークン削除")
  void resetPassword_updatesAndDeletesToken() {
    PasswordResetForm form = new PasswordResetForm();
    form.setPassword("newpass");
    form.setPasswordConfirmation("newpass");
    ResetToken rt = new ResetToken();
    rt.setEmail("user@example.com");
    when(resetTokenService.findResetTokenByToken("token")).thenReturn(rt);
    User user = new User();
    user.setPassword("old");
    when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newpass")).thenReturn("hashed");

    resetService.resetPassword("token", form);

    assertEquals("hashed", user.getPassword());
    verify(userRepository).save(user);
    verify(resetTokenService).deleteByToken("token");
  }
}
