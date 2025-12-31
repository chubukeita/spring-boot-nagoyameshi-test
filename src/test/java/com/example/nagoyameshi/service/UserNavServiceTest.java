package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.UserNavService.PreviewNext;

@ExtendWith(MockitoExtension.class)
public class UserNavServiceTest {

	@InjectMocks
	private UserNavService userNavService;

	@Mock
	private UserRepository userRepository;

	@Test
	@Description("findNeighborsNameOnly: 名前検索で前後のユーザーIDを正しく取得できること")
	public void findNeighborsNameOnly_test_1() {
		User user1 = new User();
		user1.setId(1);
		User user2 = new User();
		user2.setId(2);
		User user3 = new User();
		user3.setId(3);

		List<User> users = Arrays.asList(user1, user2, user3);
		Page<User> page = new PageImpl<>(users);

		Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
		when(userRepository.findActiveByNameLikeOrFuriganaLike("%test%", "%test%", expectedPageable))
				.thenReturn(page);

		PreviewNext result = userNavService.findNeighborsNameOnly(2, "test", "test");

		assertEquals(1, result.previewId());
		assertEquals(3, result.nextId());
		verify(userRepository).findActiveByNameLikeOrFuriganaLike("%test%", "%test%", expectedPageable);
	}

	@Test
	@Description("findNeighborsNameOnly: 最初のユーザーの場合はpreviewがnullになること")
	public void findNeighborsNameOnly_test_2() {
		User user1 = new User();
		user1.setId(1);
		User user2 = new User();
		user2.setId(2);

		List<User> users = Arrays.asList(user1, user2);
		Page<User> page = new PageImpl<>(users);

		Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
		when(userRepository.findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable)).thenReturn(page);

		PreviewNext result = userNavService.findNeighborsNameOnly(1, null, null);

		assertNull(result.previewId());
		assertEquals(2, result.nextId());
		verify(userRepository).findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable);
	}

	@Test
	@Description("findNeighborsNameOnly: 最後のユーザーの場合はnextがnullになること")
	public void findNeighborsNameOnly_test_3() {
		User user1 = new User();
		user1.setId(1);
		User user2 = new User();
		user2.setId(2);

		List<User> users = Arrays.asList(user1, user2);
		Page<User> page = new PageImpl<>(users);

		Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
		when(userRepository.findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable)).thenReturn(page);

		PreviewNext result = userNavService.findNeighborsNameOnly(2, "", "");

		assertEquals(1, result.previewId());
		assertNull(result.nextId());
		verify(userRepository).findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable);
	}

	@Test
	@Description("findNeighborsNameOnly: 存在しないIDの場合はpreviewとnextがともにnullになること")
	public void findNeighborsNameOnly_returnsBothNullWhenIdNotFound() {
		User user1 = new User();
		user1.setId(1);
		User user2 = new User();
		user2.setId(2);

		List<User> users = Arrays.asList(user1, user2);
		Page<User> page = new PageImpl<>(users);

		Pageable expectedPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
		when(userRepository.findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable)).thenReturn(page);

		PreviewNext result = userNavService.findNeighborsNameOnly(999, "", "");

		assertNull(result.previewId());
		assertNull(result.nextId());
		verify(userRepository).findActiveByNameLikeOrFuriganaLike("%", "%", expectedPageable);
	}
}
