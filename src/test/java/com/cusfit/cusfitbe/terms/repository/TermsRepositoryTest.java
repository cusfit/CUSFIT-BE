package com.cusfit.cusfitbe.terms.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.cusfit.cusfitbe.terms.entity.Terms;
import com.cusfit.cusfitbe.terms.fixture.TermsFixture;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgisContainerConfig.class)
class TermsRepositoryTest {

	@Autowired
	private TermsRepository termsRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("약관을 저장하고 재조회하면 모든 필드와 id, createdAt이 유지된다")
	void saveAndFind() {
		// given
		Terms saved = termsRepository.saveAndFlush(TermsFixture.terms());
		entityManager.clear();

		// when
		Terms found = termsRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getType()).isEqualTo(TermsFixture.TYPE);
		assertThat(found.getVersion()).isEqualTo(TermsFixture.VERSION);
		assertThat(found.getContentUrl()).isEqualTo(TermsFixture.CONTENT_URL);
		assertThat(found.getEffectiveAt()).isEqualTo(TermsFixture.EFFECTIVE_AT);
	}

	@Test
	@DisplayName("terms 테이블을 확인하면 계약된 컬럼 타입과 NOT NULL 제약을 가진다")
	void tableColumns() {
		// given
		String sql = "SELECT column_name, data_type, is_nullable FROM information_schema.columns "
				+ "WHERE table_schema = 'public' AND table_name = 'terms'";

		// when
		List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

		// then
		assertThat(columns)
				.extracting(c -> c.get("column_name") + ":" + c.get("data_type") + ":" + c.get("is_nullable"))
				.containsExactlyInAnyOrder(
						"id:bigint:NO",
						"created_at:timestamp with time zone:NO",
						"type:text:NO",
						"version:text:NO",
						"content_url:text:NO",
						"effective_at:timestamp with time zone:NO");
	}

	@Test
	@DisplayName("terms.id를 확인하면 GENERATED ALWAYS IDENTITY 컬럼이다")
	void idIsAlwaysIdentity() {
		// given
		String sql = "SELECT is_identity, identity_generation FROM information_schema.columns "
				+ "WHERE table_schema = 'public' AND table_name = 'terms' AND column_name = 'id'";

		// when
		Map<String, Object> id = jdbcTemplate.queryForMap(sql);

		// then
		assertThat(id.get("is_identity")).isEqualTo("YES");
		assertThat(id.get("identity_generation")).isEqualTo("ALWAYS");
	}

	@Test
	@DisplayName("필수 컬럼이 null인 행을 직접 넣으면 DB 제약 위반이 발생한다")
	void notNullConstraint() {
		// given
		String sql = "INSERT INTO terms (created_at, type, version, content_url, effective_at) VALUES (?, ?, ?, ?, ?)";
		Timestamp now = Timestamp.from(TermsFixture.CREATED_AT);

		// when & then
		assertThatThrownBy(() -> jdbcTemplate.update(sql, now, null, TermsFixture.VERSION,
				TermsFixture.CONTENT_URL, Timestamp.from(TermsFixture.EFFECTIVE_AT)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
