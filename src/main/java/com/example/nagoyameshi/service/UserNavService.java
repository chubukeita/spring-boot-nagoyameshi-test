package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.record.AdminUserListCond;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserNavService {
	private final UserRepository userRepository;

	public UserNavService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public record PreviewNext(Integer previewId, Integer nextId) {
	}

	public PreviewNext findNeighborsNameOnly(int currentId, String nameKeyword, String furiganaKeyword) {
		var pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));

		// findByNameLike はワイルドカード % を自分で付ける必要あり
		String namePattern = (nameKeyword == null || nameKeyword.isBlank()) ? "%" : "%" + nameKeyword + "%";
		String furiganaPattern = (furiganaKeyword == null || furiganaKeyword.isBlank()) ? "%"
				: "%" + furiganaKeyword + "%";

		Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike(namePattern, furiganaPattern, pageable);

		List<Integer> ids = page.getContent().stream().map(User::getId).toList();

		int idx = ids.indexOf(currentId);
		if (idx < 0)
			return new PreviewNext(null, null);

		Integer preview = (idx > 0) ? ids.get(idx - 1) : null;
		Integer next = (idx + 1 < ids.size()) ? ids.get(idx + 1) : null;
		return new PreviewNext(preview, next);
	}

	// 現在IDに対する「戻るURL（正しいページ）」を作成
	public String buildBackUrlForNameOnly(int currentId, AdminUserListCond cond, int pageSize, String basePath) {
		// 一覧のデフォルトに合わせる
		var pageableIdAsc = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));

		String nameKeyword = (cond != null) ? cond.keyword() : null;
		String furiganaKeyword = (cond != null) ? cond.keyword() : null;

		Page<User> page;
		if (nameKeyword == null || nameKeyword.isBlank()) {
			// キーワードなし → 全件を id ASC で
			page = userRepository.findAll(pageableIdAsc);
		} else {
			// キーワードあり → 名前部分一致を id ASC で
			String namePattern = "%" + nameKeyword + "%";
			String furiganaPattern = "%" + furiganaKeyword + "%";
			page = userRepository.findByNameLikeOrFuriganaLike(namePattern, furiganaPattern, pageableIdAsc);
		}

		List<Integer> ids = page.getContent().stream().map(User::getId).toList();
		int index = ids.indexOf(currentId);
		int pageNumber = (index >= 0) ? index / pageSize : 0;

		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(basePath)
				.queryParam("page", pageNumber)
				.queryParam("size", pageSize);

		if (nameKeyword != null && !nameKeyword.isBlank()) {
			uriComponentsBuilder.queryParam("keyword", nameKeyword);
		}
		return uriComponentsBuilder.toUriString();

	}
}