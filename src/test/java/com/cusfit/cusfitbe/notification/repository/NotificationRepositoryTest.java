package com.cusfit.cusfitbe.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.cusfit.cusfitbe.notification.entity.Notification;
import com.cusfit.cusfitbe.notification.fixture.NotificationFixture;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
class NotificationRepositoryTest {

	private static final Long MISSING_USER_ID = 999_999L;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("존재하는 수신자로 알림을 저장하고 다시 조회하면 수신자 ID, 제목, 내용, 생성 시각이 유지된다")
	void saveAndFind() {
		// given
		User recipient = userRepository.saveAndFlush(UserFixture.user());
		Notification saved = notificationRepository.saveAndFlush(NotificationFixture.notification(recipient.getId()));
		entityManager.clear();

		// when
		Notification found = notificationRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getRecipientId()).isEqualTo(recipient.getId());
		assertThat(found.getTitle()).isEqualTo(NotificationFixture.TITLE);
		assertThat(found.getContent()).isEqualTo(NotificationFixture.CONTENT);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 알림을 저장하면 FK 제약 위반이 발생한다")
	void recipientForeignKey() {
		// given
		Notification notification = NotificationFixture.notification(MISSING_USER_ID);

		// when & then
		assertThatThrownBy(() -> notificationRepository.saveAndFlush(notification))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("notifications 테이블을 확인하면 계약된 컬럼 타입과 NOT NULL 제약을 가진다")
	void notificationsTableSchema() {
		// given
		String sql = "SELECT column_name, data_type, is_nullable, is_identity, identity_generation "
				+ "FROM information_schema.columns WHERE table_schema = current_schema() "
				+ "AND table_name = 'notifications'";

		// when
		List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

		// then
		assertThat(columns).hasSize(5);
		assertThat(column(columns, "id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO")
				.containsEntry("is_identity", "YES")
				.containsEntry("identity_generation", "ALWAYS");
		assertThat(column(columns, "recipient_id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "title"))
				.containsEntry("data_type", "text")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "content"))
				.containsEntry("data_type", "text")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "created_at"))
				.containsEntry("data_type", "timestamp with time zone")
				.containsEntry("is_nullable", "NO");
	}

	private static Map<String, Object> column(List<Map<String, Object>> columns, String name) {
		return columns.stream()
				.filter(column -> name.equals(column.get("column_name")))
				.findFirst()
				.orElseThrow(() -> new AssertionError("notifications." + name + " 컬럼이 없습니다"));
	}
}
