package com.example.nagoyameshi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.RejoinEventPublisher;
import com.example.nagoyameshi.form.RejoinForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.errorMessage.RejoinResult;

@Service
public class RejoinService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RejoinTokenService rejoinTokenService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RejoinEventPublisher rejoinEventPublisher;

	// エラー検証を行うメソッド
	public RejoinResult rejoinValidate(User user, String token, RejoinToken rejoinToken) {

		if (user == null) {
			return RejoinResult.userNotFoundError();
		}

		if (user.isEnabled()) {
			return RejoinResult.alreadyEnabledError();
		}

		if (rejoinToken == null) {
			return RejoinResult.invalidTokenError();
		}

		return RejoinResult.success();
	}

	@Transactional
	public User rejoinUser(RejoinForm rejoinForm, User user) {

		user.setEmail(rejoinForm.getEmail());
		user.setPassword(passwordEncoder.encode(rejoinForm.getPassword()));
		user.setDeletedAt(null);
		user.setDeletedByUser(null);
		user.setDeleteReason(null);

		userRepository.save(user);

		return user;
	}

}
