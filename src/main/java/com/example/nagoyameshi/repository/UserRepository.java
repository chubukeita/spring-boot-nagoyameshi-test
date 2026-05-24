package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	public User findByEmail(String email);

	public Page<User> findByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable);

	public long countByRole_Name(String roleName);

	public Optional<User> findByEmailAndDeletedAtIsNull(String email);

	// メールアドレスが一致する退会ユーザーを取得する
	public Optional<User> findByEmailAndDeletedAtIsNotNull(String email);

	@Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
	Page<User> findAllActive(Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND (u.name LIKE :nameKeyword OR u.furigana LIKE :furiganaKeyword)")
	public Page<User> findActiveByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable);

	@Query("SELECT u FROM User u ORDER BY u.deletedAt ASC, u.id ASC")
	public List<User> findAllIncludeDeleted(); // 管理一覧用（退会含む）
}
