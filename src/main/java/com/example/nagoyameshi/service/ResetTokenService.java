package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.ResetToken;
import com.example.nagoyameshi.repository.ResetTokenRepository;

@Service
public class ResetTokenService {

	private final ResetTokenRepository resetTokenRepository;

	public ResetTokenService(ResetTokenRepository resetTokenRepository) {
		this.resetTokenRepository = resetTokenRepository;
	}

	@Transactional
	public void createResetToken(String email, String token) {
		ResetToken resetToken = new ResetToken();

		resetToken.setEmail(email);
		resetToken.setToken(token);

		resetTokenRepository.save(resetToken);
	}

	// トークン削除
	@Transactional
	public void deleteByToken(String token) {
		resetTokenRepository.deleteByToken(token);
	}

	// トークンの文字列で検索した結果を返す
	public ResetToken findResetTokenByToken(String token) {
		return resetTokenRepository.findByToken(token);
	}

}
