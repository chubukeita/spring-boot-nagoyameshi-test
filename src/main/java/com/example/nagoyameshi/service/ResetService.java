package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.ResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.ResetEventPublisher;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.InvalidTokenException;
import com.example.nagoyameshi.service.error.PasswordMismatchException;
import com.example.nagoyameshi.service.error.UserNotFoundForTokenException;

@Service
public class ResetService {
	private final ResetTokenService resetTokenService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ResetEventPublisher resetEventPublisher;

	public ResetService(ResetTokenService resetTokenService, UserRepository userRepository,
			PasswordEncoder passwordEncoder, ResetEventPublisher resetEventPublisher) {
		this.resetTokenService = resetTokenService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.resetEventPublisher = resetEventPublisher;
	}

	// リクエスト受付（存在可否は秘匿し、常に同じレスポンスでOK）
	public void requestReset(String email, String requestUrl) {
		resetEventPublisher.publishResetEvent(email, requestUrl);
	}

	// 画面表示用にトークン有効かだけ返す
	public boolean isValidToken(String token) {
		return resetTokenService.findResetTokenByToken(token) != null;
	}

	// 実処理：検証→更新→トークン削除
	@Transactional
	public void resetPassword(String token, PasswordResetForm form) {

		if (!form.getPassword().equals(form.getPasswordConfirmation())) {
			throw new PasswordMismatchException();
		}

		ResetToken resetToken = resetTokenService.findResetTokenByToken(token);

		if (resetToken == null) {
			throw new InvalidTokenException();
		}

		Optional<User> optionalUser = userRepository.findByEmailAndDeletedAtIsNull(resetToken.getEmail());
		User user = optionalUser.orElseThrow(UserNotFoundForTokenException::new);

		user.setPassword(passwordEncoder.encode(form.getPassword()));
		userRepository.save(user);

		resetTokenService.deleteByToken(token);
	}
}
