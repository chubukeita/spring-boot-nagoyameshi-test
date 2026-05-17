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

import com.example.nagoyameshi.entity.ResetToken;

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
@ActiveProfiles("test")
public class ResetTokenRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ResetTokenRepository resetTokenRepository;

	private ResetToken resetToken;

	@BeforeEach
	void setUp() {
		resetToken = new ResetToken();
		resetToken.setEmail("reset@example.com");
		resetToken.setToken("reset-token");
		resetToken = entityManager.persist(resetToken);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findByToken_トークンで取得できること")
	void findByToken_test_1() {
		ResetToken found = resetTokenRepository.findByToken("reset-token");

		assertNotNull(found);
		assertEquals(resetToken.getId(), found.getId());
		assertEquals("reset@example.com", found.getEmail());
	}

	@Test
	@Description("deleteByToken_トークンで削除できること")
	void deleteByToken_test_1() {
		resetTokenRepository.deleteByToken("reset-token");

		ResetToken deleted = resetTokenRepository.findByToken("reset-token");
		assertNull(deleted);
	}
}
