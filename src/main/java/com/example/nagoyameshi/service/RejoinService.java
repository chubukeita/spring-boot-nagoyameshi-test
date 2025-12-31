package com.example.nagoyameshi.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.AlreadyEnabledException;
import com.example.nagoyameshi.service.error.InvalidTokenException;
import com.example.nagoyameshi.service.error.RejoinUserNotFoundException;

@Service
public class RejoinService {
	private final RejoinTokenService rejoinTokenService;
	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RejoinEventPublisher rejoinEventPublisher;

	public RejoinService(RejoinTokenService rejoinTokenService, UserService userService, UserRepository userRepository,
			PasswordEncoder passwordEncoder, RejoinEventPublisher rejoinEventPublisher) {
		this.rejoinTokenService = rejoinTokenService;
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.rejoinEventPublisher = rejoinEventPublisher;
	}

	// リクエスト受付（存在可否は秘匿し、常に同じレスポンスでOK）
	public void requestRejoin(String email, String requestUrl) {
		rejoinEventPublisher.publishRejoinEvent(email, requestUrl);
	}

	// 画面表示用にトークン有効かだけ返す
	public boolean isValidToken(String token) {
		return rejoinTokenService.findRejoinTokenByToken(token) != null;
	}

	// 		if (rejoinToken == null) {
	//	String errorMessage = "トークンが無効です。恐れ入りますが、再度メール認証からやり直してください。";
	//	model.addAttribute("errorMessage", errorMessage);
	//	return "auth/invalid";
	//}

	// void→String エラーの時はstring 成功時はnull?? 
	// returnでメッセージと結果を含んだクラスを作って、(rejoin)Resultオブジェクトをbooleanでfalse、成功時trueを返す。
	// errorパッケージも消して、RejoinControllerのsuccessMessageやerrorMessageはまとめておく
	// 一つのクラスにまとめてstaticで呼ぶ。

	@Transactional
	public void rejoin(String token) {
		RejoinToken rejoinToken = rejoinTokenService.findRejoinTokenByToken(token);

		if (rejoinToken == null) {
			throw new InvalidTokenException();
		}

		User user = userService.findUserByEmail(rejoinToken.getEmail());

		// model.setAttributeで渡す（）
		// 例外ハンドラーを作る。
		if (user == null) {
			throw new RejoinUserNotFoundException();
		}

		if (user.getEnabled()) {
			throw new AlreadyEnabledException();
		}

		user.setDeletedAt(null);
		user.setDeletedByUser(null);
		user.setDeleteReason(null);
		userService.enableUser(user);

		userRepository.save(user);
		rejoinTokenService.deleteByToken(token);
	}
}
