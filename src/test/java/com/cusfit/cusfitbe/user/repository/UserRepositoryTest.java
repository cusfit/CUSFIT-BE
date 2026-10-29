package com.cusfit.cusfitbe.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import jakarta.persistence.EntityManager;
import java.time.temporal.ChronoUnit;
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
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("사용자를 저장한 뒤 다시 조회하면 id, 닉네임, 생성 시각이 유지된다")
	void saveAndFind() {
		// given
		User saved = userRepository.save(UserFixture.user());
		entityManager.flush();
		entityManager.clear();

		// when
		User found = userRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getNickname()).isEqualTo(UserFixture.NICKNAME);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getCreatedAt()).isCloseTo(saved.getCreatedAt(), within(1, ChronoUnit.MILLIS));
	}

	@Test
	@DisplayName("users 테이블을 확인하면 계약된 컬럼 타입과 NOT NULL 제약을 가진다")
	void usersTableSchema() {
		// given
		String sql = "SELECT column_name, data_type, is_nullable, is_identity, identity_generation "
				+ "FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'users'";

		// when
		List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

		// then
		assertThat(columns).hasSize(3);
		assertThat(column(columns, "id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO")
				.containsEntry("is_identity", "YES")
				.containsEntry("identity_generation", "ALWAYS");
		assertThat(column(columns, "nickname"))
				.containsEntry("data_type", "text")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "created_at"))
				.containsEntry("data_type", "timestamp with time zone")
				.containsEntry("is_nullable", "NO");
	}

	@Test
	@DisplayName("닉네임 없이 users에 직접 삽입하면 NOT NULL 제약으로 실패한다")
	void nicknameNotNullConstraint() {
		// given
		String sql = "INSERT INTO users (nickname, created_at) VALUES (NULL, now())";

		// when & then
		assertThatThrownBy(() -> jdbcTemplate.update(sql))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private static Map<String, Object> column(List<Map<String, Object>> columns, String name) {
		return columns.stream()
				.filter(column -> name.equals(column.get("column_name")))
				.findFirst()
				.orElseThrow(() -> new AssertionError("users." + name + " 컬럼이 없습니다"));
	}
}
