package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.repository.RejoinTokenRepository;

@ExtendWith(MockitoExtension.class)
public class RejoinTokenServiceTest {

  @Mock
  private RejoinTokenRepository rejoinTokenRepository;

  @InjectMocks
  private RejoinTokenService rejoinTokenService;

  @Test
  @Description("createRejoinToken_メールアドレスとトークンで再入会トークンを作成し保存すること")
  public void createRejoinToken_test_1() {
    rejoinTokenService.createRejoinToken("user@example.com", "token");

    RejoinToken expected = new RejoinToken();
    expected.setEmail("user@example.com");
    expected.setToken("token");
    verify(rejoinTokenRepository).save(expected);
  }

  @Test
  @Description("deleteByToken_トークンを指定して削除処理が呼ばれること")
  public void deleteByToken_test_1() {
    rejoinTokenService.deleteByToken("token");

    verify(rejoinTokenRepository).deleteByToken("token");
  }

  @Test
  @Description("findRejoinTokenByToken_トークンで検索して結果を返すこと")
  public void findRejoinTokenByToken_test_1() {
    RejoinToken token = new RejoinToken();
    when(rejoinTokenRepository.findByToken("token")).thenReturn(token);

    RejoinToken result = rejoinTokenService.findRejoinTokenByToken("token");

    assertEquals(token, result);
    verify(rejoinTokenRepository).findByToken("token");
  }
}
