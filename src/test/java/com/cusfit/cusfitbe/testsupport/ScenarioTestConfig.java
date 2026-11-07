package com.cusfit.cusfitbe.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** 시나리오 테스트 공용 빈. */
@TestConfiguration(proxyBeanMethods = false)
public class ScenarioTestConfig {

	@Bean
	DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
		return new DatabaseCleaner(jdbcTemplate);
	}
}
