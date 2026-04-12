package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ActiveProfiles;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;

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
@ActiveProfiles("repository-test")
public class VerificationTokenRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	private VerificationToken verificationToken;

	@BeforeEach
	void setUp() {
		Role role = new Role();
		role.setName("ROLE_FREE_MEMBER");
		role = entityManager.persist(role);

		User user = new User();
		user.setName("テストユーザー");
		user.setFurigana("テストユーザー");
		user.setPostalCode("4600000");
		user.setAddress("名古屋市中区");
		user.setPhoneNumber("09000000000");
		user.setEmail("verify@example.com");
		user.setPassword("password");
		user.setRole(role);
		user.setEnabled(true);
		user = entityManager.persist(user);

		verificationToken = new VerificationToken();
		verificationToken.setUser(user);
		verificationToken.setToken("verify-token");
		verificationToken = entityManager.persist(verificationToken);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findByToken_トークンで取得できること")
	void findByToken_test_1() {
		VerificationToken found = verificationTokenRepository.findByToken("verify-token");

		assertNotNull(found);
		assertEquals(verificationToken.getId(), found.getId());
		assertEquals("verify-token", found.getToken());
		assertEquals("verify@example.com", found.getUser().getEmail());
	}
}
