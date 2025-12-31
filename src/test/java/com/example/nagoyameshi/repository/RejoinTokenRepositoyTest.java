package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.TestPropertySource;

import com.example.nagoyameshi.entity.RejoinToken;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RejoinTokenRepositoyTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RejoinTokenRepository rejoinTokenRepository;

  private RejoinToken rejoinToken;

  @BeforeEach
  void setUp() {
    rejoinToken = new RejoinToken();
    rejoinToken.setEmail("rejoin@example.com");
    rejoinToken.setToken("rejoin-token");
    rejoinToken = entityManager.persist(rejoinToken);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @Description("findByToken_トークンで取得できること")
  void findByToken_returnsEntity() {
    RejoinToken found = rejoinTokenRepository.findByToken("rejoin-token");

    assertNotNull(found);
    assertEquals(rejoinToken.getId(), found.getId());
    assertEquals("rejoin@example.com", found.getEmail());
  }

  @Test
  @Description("deleteByToken_トークンで削除できること")
  void deleteByToken_removesEntity() {
    rejoinTokenRepository.deleteByToken("rejoin-token");

    assertNull(rejoinTokenRepository.findByToken("rejoin-token"));
  }
}
