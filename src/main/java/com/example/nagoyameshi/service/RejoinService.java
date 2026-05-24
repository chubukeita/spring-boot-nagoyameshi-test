package com.example.nagoyameshi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.errorMessage.RejoinResult;

@Service
public class RejoinService {
	@Autowired
	private RejoinTokenService rejoinTokenService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RejoinEventPublisher rejoinEventPublisher;

	// リクエスト受付（存在可否は秘匿し、常に同じレスポンスでOK）
	public void requestRejoin(String email, String requestUrl) {
		rejoinEventPublisher.publishRejoinEvent(email, requestUrl);
	}

	// 画面表示用にトークン有効かだけ返す
	public boolean isValidToken(String token) {
		return rejoinTokenService.findRejoinTokenByToken(token) != null;
	}

	@Transactional
	public RejoinResult rejoin(String token) {
		RejoinToken rejoinToken = rejoinTokenService.findRejoinTokenByToken(token);

		if (rejoinToken == null) {
			return RejoinResult.invalidTokenError();
		}

		User user = userService.findUserByEmail(rejoinToken.getEmail());
		if (user == null) {
			return RejoinResult.userNotFoundError();
		}

		if (user.isEnabled()) {
			return RejoinResult.alreadyEnabledError();
		}

		user.setDeletedAt(null);
		user.setDeletedByUser(null);
		user.setDeleteReason(null);
		userService.enableUser(user);

		userRepository.save(user);
		rejoinTokenService.deleteByToken(token);

		return RejoinResult.success();
	}
}
