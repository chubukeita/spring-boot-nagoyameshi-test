package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.RejoinToken;
import com.example.nagoyameshi.entity.User;

public interface RejoinTokenRepository extends JpaRepository<RejoinToken, Integer> {
	RejoinToken findByToken(String token);

	RejoinToken findByUser(User user);

}
