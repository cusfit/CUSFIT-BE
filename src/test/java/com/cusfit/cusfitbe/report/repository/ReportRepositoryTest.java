package com.cusfit.cusfitbe.report.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.cusfit.cusfitbe.recipe.entity.Recipe;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.recipe.repository.RecipeRepository;
import com.cusfit.cusfitbe.report.entity.Report;
import com.cusfit.cusfitbe.report.fixture.ReportFixture;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
class ReportRepositoryTest {

	private static final Long MISSING_ID = 999_999L;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Long reporterId;
	private Long recipeId;

	@BeforeEach
	void setUp() {
		User user = userRepository.saveAndFlush(UserFixture.user());
		Brand brand = brandRepository.saveAndFlush(CatalogFixture.brand());
		Menu menu = menuRepository.saveAndFlush(CatalogFixture.menu(brand.getId()));
		Recipe recipe = recipeRepository.saveAndFlush(
				RecipeFixture.recipe(user.getId(), menu.getId(), RecipeFixture.OPTION_NAMES));
		reporterId = user.getId();
		recipeId = recipe.getId();
	}

	@Test
	@DisplayName("신고를 저장한 뒤 다시 조회하면 id, 신고자, 레시피, 내용, 생성 시각이 유지된다")
	void saveAndFind() {
		// given
		Report saved = reportRepository.saveAndFlush(ReportFixture.report(reporterId, recipeId));
		entityManager.clear();

		// when
		Report found = reportRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getReporterId()).isEqualTo(reporterId);
		assertThat(found.getRecipeId()).isEqualTo(recipeId);
		assertThat(found.getContent()).isEqualTo(ReportFixture.CONTENT);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("reports 테이블을 확인하면 계약된 컬럼 타입과 NOT NULL 제약을 가진다")
	void reportsTableSchema() {
		// given
		String sql = "SELECT column_name, data_type, is_nullable, is_identity, identity_generation "
				+ "FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'reports'";

		// when
		List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

		// then
		assertThat(columns).hasSize(5);
		assertThat(column(columns, "id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO")
				.containsEntry("is_identity", "YES")
				.containsEntry("identity_generation", "ALWAYS");
		assertThat(column(columns, "reporter_id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "recipe_id"))
				.containsEntry("data_type", "bigint")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "content"))
				.containsEntry("data_type", "text")
				.containsEntry("is_nullable", "NO");
		assertThat(column(columns, "created_at"))
				.containsEntry("data_type", "timestamp with time zone")
				.containsEntry("is_nullable", "NO");
	}

	@Test
	@DisplayName("존재하지 않는 신고자 ID로 신고를 저장하면 FK 제약 위반이 발생한다")
	void reporterForeignKey() {
		// given
		Report report = ReportFixture.report(MISSING_ID, recipeId);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> reportRepository.saveAndFlush(report));
	}

	@Test
	@DisplayName("존재하지 않는 레시피 ID로 신고를 저장하면 FK 제약 위반이 발생한다")
	void recipeForeignKey() {
		// given
		Report report = ReportFixture.report(reporterId, MISSING_ID);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> reportRepository.saveAndFlush(report));
	}

	@Test
	@DisplayName("신고가 참조하는 레시피를 DB에서 삭제하면 FK 제약 위반이 발생한다")
	void recipeDeleteNoCascade() {
		// given
		reportRepository.saveAndFlush(ReportFixture.report(reporterId, recipeId));
		entityManager.clear();

		// when & then
		assertThrows(DataIntegrityViolationException.class,
				() -> jdbcTemplate.update("DELETE FROM recipes WHERE id = ?", recipeId));
	}

	private static Map<String, Object> column(List<Map<String, Object>> columns, String name) {
		return columns.stream()
				.filter(column -> name.equals(column.get("column_name")))
				.findFirst()
				.orElseThrow(() -> new AssertionError("reports." + name + " 컬럼이 없습니다"));
	}
}
