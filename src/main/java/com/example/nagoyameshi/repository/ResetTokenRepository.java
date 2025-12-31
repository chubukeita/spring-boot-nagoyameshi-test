package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.ResetToken;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Integer> {
	ResetToken findByToken(String token);

	void deleteByToken(String token);
}
