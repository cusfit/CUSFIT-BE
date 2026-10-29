package com.cusfit.cusfitbe.support.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cusfit.cusfitbe.support.PostgisContainerConfig;
import com.cusfit.cusfitbe.support.entity.Faq;
import com.cusfit.cusfitbe.support.entity.Inquiry;
import com.cusfit.cusfitbe.support.entity.Notice;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgisContainerConfig.class)
class SupportRepositoryTest {

	private static final Long MISSING_USER_ID = 999_999L;

	@Autowired
	private NoticeRepository noticeRepository;

	@Autowired
	private FaqRepository faqRepository;

	@Autowired
	private InquiryRepository inquiryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("공지를 저장하고 다시 조회하면 제목, 내용, 생성 시각이 유지된다")
	void saveAndFindNotice() {
		// given
		Notice saved = noticeRepository.saveAndFlush(SupportFixture.notice());
		entityManager.clear();

		// when
		Notice found = noticeRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getTitle()).isEqualTo(SupportFixture.NOTICE_TITLE);
		assertThat(found.getContent()).isEqualTo(SupportFixture.NOTICE_CONTENT);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("FAQ를 저장하고 다시 조회하면 질문, 답변, 생성 시각이 유지된다")
	void saveAndFindFaq() {
		// given
		Faq saved = faqRepository.saveAndFlush(SupportFixture.faq());
		entityManager.clear();

		// when
		Faq found = faqRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getQuestion()).isEqualTo(SupportFixture.FAQ_QUESTION);
		assertThat(found.getAnswer()).isEqualTo(SupportFixture.FAQ_ANSWER);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하는 작성자로 문의를 저장하고 다시 조회하면 작성자 ID, 제목, 내용, 생성 시각이 유지된다")
	void saveAndFindInquiry() {
		// given
		User author = userRepository.saveAndFlush(UserFixture.user());
		Inquiry saved = inquiryRepository.saveAndFlush(SupportFixture.inquiry(author.getId()));
		entityManager.clear();

		// when
		Inquiry found = inquiryRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getAuthorId()).isEqualTo(author.getId());
		assertThat(found.getTitle()).isEqualTo(SupportFixture.INQUIRY_TITLE);
		assertThat(found.getContent()).isEqualTo(SupportFixture.INQUIRY_CONTENT);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 문의를 저장하면 FK 제약 위반이 발생한다")
	void inquiryAuthorForeignKey() {
		// given
		Inquiry inquiry = SupportFixture.inquiry(MISSING_USER_ID);

		// when & then
		assertThatThrownBy(() -> inquiryRepository.saveAndFlush(inquiry))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"notices:title,content", "faqs:question,answer", "inquiries:title,content"})
	@DisplayName("support 테이블을 확인하면 IDENTITY PK, text NOT NULL 컬럼, timestamptz 생성 시각을 가진다")
	void tableSchema(String spec) {
		// given
		String table = spec.split(":")[0];
		List<String> textColumns = List.of(spec.split(":")[1].split(","));

		// when
		List<Map<String, Object>> columns = columnsOf(table);

		// then
		int expectedSize = 2 + textColumns.size() + (table.equals("inquiries") ? 1 : 0);
		assertThat(columns).hasSize(expectedSize);
		assertThat(column(columns, table, "id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO")
				.containsEntry("is_identity", "YES")
				.containsEntry("identity_generation", "ALWAYS");
		for (String textColumn : textColumns) {
			assertThat(column(columns, table, textColumn))
					.containsEntry("data_type", "text")
					.containsEntry("is_nullable", "NO");
		}
		assertThat(column(columns, table, "created_at"))
				.containsEntry("data_type", "timestamp with time zone")
				.containsEntry("is_nullable", "NO");
	}

	@Test
	@DisplayName("inquiries 테이블의 작성자 ID는 bigint NOT NULL이고 users를 참조한다")
	void inquiryAuthorColumn() {
		// given
		String fkSql = "SELECT ccu.table_name AS referenced_table, ccu.column_name AS referenced_column "
				+ "FROM information_schema.table_constraints tc "
				+ "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name "
				+ "AND tc.table_schema = kcu.table_schema "
				+ "JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name "
				+ "AND tc.table_schema = ccu.table_schema "
				+ "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = current_schema() "
				+ "AND tc.table_name = 'inquiries' AND kcu.column_name = 'author_id'";

		// when
		List<Map<String, Object>> columns = columnsOf("inquiries");
		List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(fkSql);

		// then
		assertThat(column(columns, "inquiries", "author_id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO");
		assertThat(foreignKeys).singleElement()
				.satisfies(fk -> assertThat(fk)
						.containsEntry("referenced_table", "users")
						.containsEntry("referenced_column", "id"));
	}

	private List<Map<String, Object>> columnsOf(String table) {
		String sql = "SELECT column_name, data_type, is_nullable, is_identity, identity_generation "
				+ "FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = ?";
		return jdbcTemplate.queryForList(sql, table);
	}

	private static Map<String, Object> column(List<Map<String, Object>> columns, String table, String name) {
		return columns.stream()
				.filter(column -> name.equals(column.get("column_name")))
				.findFirst()
				.orElseThrow(() -> new AssertionError(table + "." + name + " 컬럼이 없습니다"));
	}
}
