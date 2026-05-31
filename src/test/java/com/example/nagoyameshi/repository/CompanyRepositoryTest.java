package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.Company;

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
public class CompanyRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CompanyRepository companyRepository;

	private Company company1;
	private Company company2;

	@BeforeEach
	void setUp() {
		company1 = new Company();
		company1.setName("テスト会社1");
		company1.setPostalCode("1010022");
		company1.setAddress("東京都千代田区");
		company1.setRepresentative("代表者A");
		company1.setEstablishmentDate("2000年1月1日");
		company1.setCapital("1000万円");
		company1.setBusiness("IT");
		company1.setNumberOfEmployees("10人");
		company1 = entityManager.persist(company1);

		company2 = new Company();
		company2.setName("テスト会社2");
		company2.setPostalCode("1500002");
		company2.setAddress("東京都渋谷区");
		company2.setRepresentative("代表者B");
		company2.setEstablishmentDate("2010年1月1日");
		company2.setCapital("500万円");
		company2.setBusiness("コンサル");
		company2.setNumberOfEmployees("5人");
		company2 = entityManager.persist(company2);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@Description("findFirstByOrderByIdDesc_一番IDが大きい会社が取得できること")
	void findFirstByOrderByIdDesc_test_1() {
		Company found = companyRepository.findFirstByOrderByIdDesc();

		assertNotNull(found);
		assertEquals(company2.getId(), found.getId());
		assertEquals("テスト会社2", found.getName());
	}
}
