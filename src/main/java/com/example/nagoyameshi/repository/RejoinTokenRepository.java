package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.RejoinToken;

public interface RejoinTokenRepository extends JpaRepository<RejoinToken, Integer> {
	RejoinToken findByToken(String token);

	void deleteByToken(String token);
}
