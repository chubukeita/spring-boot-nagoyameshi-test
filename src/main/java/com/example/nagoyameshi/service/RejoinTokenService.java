package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.repository.RejoinTokenRepository;

@Service
public class RejoinTokenService {

	private final RejoinTokenRepository rejoinTokenRepository;

	public RejoinTokenService(RejoinTokenRepository rejoinTokenRepository) {
		this.rejoinTokenRepository = rejoinTokenRepository;
	}

	@Transactional
	public void createRejoinToken(String email, String token) {
		RejoinToken rejoinToken = new RejoinToken();

		rejoinToken.setEmail(email);
		rejoinToken.setToken(token);

		rejoinTokenRepository.save(rejoinToken);
	}

	// トークン削除
	@Transactional
	public void deleteByToken(String token) {
		rejoinTokenRepository.deleteByToken(token);
	}

	// トークンの文字列で検索した結果を返す
	public RejoinToken findRejoinTokenByToken(String token) {
		return rejoinTokenRepository.findByToken(token);
	}

}
