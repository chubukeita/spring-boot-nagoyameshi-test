package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.Role;

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
public class RoleRepositoryTest {
	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private RoleRepository roleRepository;

	private Role testRole;

	@BeforeEach
	void setUp() {
		testRole = new Role();
		testRole.setName("ROLE_TEST");
		entityManager.persist(testRole);
		entityManager.flush();
	}

	@Test
	@Description("findByName でロール名から正しくロールが取得できる")
	public void findByName_test_1() {
		// 実行：findByName でロール名を指定して検索
		Role foundRole = roleRepository.findByName("ROLE_TEST");

		// 検証：取得したロールが期待通りであることを確認
		assertNotNull(foundRole);
		assertEquals("ROLE_TEST", foundRole.getName());
	}

	@Test
	@Description("findByName で存在しないロール名を指定すると null が返される")
	public void findByName_test_2() {
		// 実行：存在しないロール名で検索
		Role foundRole = roleRepository.findByName("ROLE_NONEXISTENT");

		// 検証：null が返されることを確認
		assertNull(foundRole);
	}
}
