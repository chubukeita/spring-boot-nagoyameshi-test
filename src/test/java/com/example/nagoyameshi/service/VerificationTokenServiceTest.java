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

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  @InjectMocks
  private VerificationTokenService verificationTokenService;

  @Test
  @Description("createVerificationToken_ユーザーとトークンで保存すること")
  public void createVerificationToken_test_1() {
    User user = new User();
    user.setId(1);

    verificationTokenService.createVerificationToken(user, "token");

    VerificationToken expected = new VerificationToken();
    expected.setUser(user);
    expected.setToken("token");
    verify(verificationTokenRepository).save(expected);
  }

  @Test
  @Description("findVerificationTokenByToken_トークンで検索すること")
  public void findVerificationTokenByToken_test_1() {
    VerificationToken token = new VerificationToken();
    when(verificationTokenRepository.findByToken("token")).thenReturn(token);

    VerificationToken result = verificationTokenService.findVerificationTokenByToken("token");

    assertEquals(token, result);
    verify(verificationTokenRepository).findByToken("token");
  }
}
