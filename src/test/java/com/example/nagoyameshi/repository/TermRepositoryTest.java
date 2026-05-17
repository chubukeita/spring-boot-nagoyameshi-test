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

import com.example.nagoyameshi.entity.Term;

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
public class TermRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TermRepository termRepository;

	private Term oldTerm;
	private Term newTerm;

	@BeforeEach
	void setUp() {
		oldTerm = new Term();
		oldTerm.setContent("古い利用規約");
		oldTerm = entityManager.persist(oldTerm);

		newTerm = new Term();
		newTerm.setContent("新しい利用規約");
		newTerm = entityManager.persist(newTerm);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findFirstByOrderByIdDesc_最新の利用規約を返すこと")
	void findFirstByOrderByIdDesc_test_1() {
		Term found = termRepository.findFirstByOrderByIdDesc();

		assertNotNull(found);
		assertEquals(newTerm.getId(), found.getId());
		assertEquals("新しい利用規約", found.getContent());
	}
}
