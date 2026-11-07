package com.cusfit.cusfitbe.testsupport;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 시나리오 테스트는 {@code @Transactional} 롤백 없이 실제로 커밋하므로, 테스트가 끝나면 테이블을 비워 격리한다.
 * Flyway 이력과 PostGIS 시스템 테이블은 남긴다.
 */
public class DatabaseCleaner {

	private static final List<String> KEEP_TABLES = List.of("flyway_schema_history", "spatial_ref_sys");

	private final JdbcTemplate jdbcTemplate;

	public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void clean() {
		List<String> tables = jdbcTemplate.queryForList(
				"SELECT tablename FROM pg_tables WHERE schemaname = 'public'", String.class);
		List<String> targets = tables.stream().filter(table -> !KEEP_TABLES.contains(table)).toList();
		if (targets.isEmpty()) {
			return;
		}
		jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", targets) + " RESTART IDENTITY CASCADE");
	}
}
