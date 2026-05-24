package com.example.nagoyameshi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = {
		"classpath:application.properties",
		"classpath:application-test.properties"
})
class NagoyameshiApplicationTests {

	@Test
	void contextLoads() {
	}

}
