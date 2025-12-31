package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.AlreadyEnabledException;
import com.example.nagoyameshi.service.error.InvalidTokenException;
import com.example.nagoyameshi.service.error.RejoinUserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RejoinServiceTest {

  @Mock
  private RejoinTokenService rejoinTokenService;

  @Mock
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder; // constructor dependency

  @Mock
  private RejoinEventPublisher rejoinEventPublisher;

  @InjectMocks
  private RejoinService rejoinService;

  @Test
  @Description("requestRejoin_再入会用のURLを含むイベントが発行されること")
  public void requestRejoin_test_1() {
    rejoinService.requestRejoin("user@example.com", "http://example/rejoin");

    verify(rejoinEventPublisher)
        .publishRejoinEvent("user@example.com", "http://example/rejoin");
  }

  @Test
  @Description("isValidToken_トークンが存在する場合はtrueを返すこと")
  public void isValidToken_test_1() {
    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(new RejoinToken());

    assertTrue(rejoinService.isValidToken("token"));
  }

  @Test
  @Description("isValidToken_トークンが存在しない場合はfalseを返すこと")
  public void isValidToken_test_2() {
    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(null);

    assertFalse(rejoinService.isValidToken("token"));
  }

  @Test
  @Description("rejoin_無効なトークンの場合は例外が送出されること")
  public void rejoin_test_1() {
    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(null);

    assertThrows(
        InvalidTokenException.class,
        () -> rejoinService.rejoin("token"));
  }

  @Test
  @Description("rejoin_ユーザーが存在しない場合は例外が送出されること")
  public void rejoin_test_2() {
    RejoinToken token = new RejoinToken();
    token.setEmail("user@example.com");

    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(token);
    when(userService.findUserByEmail("user@example.com"))
        .thenReturn(null);

    assertThrows(
        RejoinUserNotFoundException.class,
        () -> rejoinService.rejoin("token"));
  }

  @Test
  @Description("rejoin_すでに有効化されているユーザーの場合は例外が送出されること")
  public void rejoin_test_3() {
    RejoinToken token = new RejoinToken();
    token.setEmail("user@example.com");

    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(token);

    User user = new User();
    user.setEnabled(true);

    when(userService.findUserByEmail("user@example.com"))
        .thenReturn(user);

    assertThrows(
        AlreadyEnabledException.class,
        () -> rejoinService.rejoin("token"));
  }

  @Test
  @Description("rejoin_正常系：ユーザーが再有効化され、トークンが削除されること")
  public void rejoin_test_4() {
    RejoinToken token = new RejoinToken();
    token.setEmail("user@example.com");

    when(rejoinTokenService.findRejoinTokenByToken("token"))
        .thenReturn(token);

    User user = new User();
    user.setEnabled(false);
    user.setDeletedAt(java.time.LocalDateTime.now());
    user.setDeletedByUser(true);
    user.setDeleteReason("reason");

    when(userService.findUserByEmail("user@example.com"))
        .thenReturn(user);

    rejoinService.rejoin("token");

    assertNull(user.getDeletedAt());
    assertNull(user.getDeletedByUser());
    assertNull(user.getDeleteReason());

    verify(userService).enableUser(user);
    verify(userRepository).save(user);
    verify(rejoinTokenService).deleteByToken("token");
  }
}
