package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.RejoinTokenRepository;

@Service
public class RejoinTokenService {

	private final RejoinTokenRepository rejoinTokenRepository;

	public RejoinTokenService(RejoinTokenRepository rejoinTokenRepository) {
		this.rejoinTokenRepository = rejoinTokenRepository;
	}

	// トークンの文字列で検索した結果を返す
	public RejoinToken findRejoinTokenByToken(String token) {
		return rejoinTokenRepository.findByToken(token);
	}

	@Transactional
	public void createRejoinToken(User user, String token) {
		RejoinToken rejoinToken = rejoinTokenRepository.findByUser(user);
		if (rejoinToken == null) {
			rejoinToken = new RejoinToken();
			rejoinToken.setUser(user);
		}
		rejoinToken.setToken(token);

		rejoinTokenRepository.save(rejoinToken);
	}
}
