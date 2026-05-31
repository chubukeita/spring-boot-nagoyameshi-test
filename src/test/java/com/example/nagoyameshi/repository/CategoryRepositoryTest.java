package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.nagoyameshi.entity.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = {
//		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1",
//		"spring.datasource.driverClassName=org.h2.Driver",
//		"spring.datasource.username=sa",
//		"spring.datasource.password=",
//		"spring.sql.init.mode=never",
//		"spring.jpa.hibernate.ddl-auto=create-drop"
//})
// @ActiveProfiles("test")
public class CategoryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CategoryRepository categoryRepository;

	private Category izakaya;
	private Category sushi;

	@BeforeEach
	void setUp() {
		izakaya = new Category();
		izakaya.setName("居酒屋");
		izakaya = entityManager.persist(izakaya);

		sushi = new Category();
		sushi.setName("寿司");
		sushi = entityManager.persist(sushi);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findByNameLike_部分一致でカテゴリをページング取得できること")
	void findByNameLike_test_1() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<Category> page = categoryRepository.findByNameLike("%寿%", pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("寿司", page.getContent().get(0).getName());
	}

	@Test
	@Description("findFirstByOrderByIdDesc_IDが最大のカテゴリを取得できること")
	void findFirstByOrderByIdDesc_test_1() {
		Category latest = categoryRepository.findFirstByOrderByIdDesc();

		assertNotNull(latest);
		assertEquals(sushi.getId(), latest.getId());
		assertEquals("寿司", latest.getName());
	}

	@Test
	@Description("findFirstByName_名称一致でカテゴリを1件取得できること")
	void findFirstByName_test_1() {
		Category found = categoryRepository.findFirstByName("居酒屋");

		assertNotNull(found);
		assertEquals(izakaya.getId(), found.getId());
		assertEquals("居酒屋", found.getName());
	}

	@Test
	@Description("findAll_すべてのカテゴリを一覧取得できること")
	void findAll_test_1() {
		List<Category> categories = categoryRepository.findAll();

		assertEquals(2, categories.size());
		assertTrue(categories.stream().anyMatch(c -> "居酒屋".equals(c.getName())));
		assertTrue(categories.stream().anyMatch(c -> "寿司".equals(c.getName())));
	}
}
