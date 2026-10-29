package com.cusfit.cusfitbe.recipe.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.cusfit.cusfitbe.recipe.entity.Recipe;
import com.cusfit.cusfitbe.recipe.entity.RecipeOption;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
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
class RecipeRepositoryTest {

	private static final Long MISSING_ID = 999_999L;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Long authorId;
	private Long menuId;

	@BeforeEach
	void setUp() {
		User author = userRepository.saveAndFlush(UserFixture.user());
		Brand brand = brandRepository.saveAndFlush(CatalogFixture.brand());
		Menu menu = menuRepository.saveAndFlush(CatalogFixture.menu(brand.getId()));
		authorId = author.getId();
		menuId = menu.getId();
	}

	@Test
	@DisplayName("레시피를 저장한 뒤 다시 조회하면 필드와 옵션의 ID, 이름, 위치, 생성 시각이 순서대로 유지된다")
	void saveAndFindWithOptions() {
		// given
		Recipe saved = recipeRepository.saveAndFlush(RecipeFixture.recipe(authorId, menuId, List.of("C", "A", "B")));
		entityManager.clear();

		// when
		Recipe found = recipeRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getAuthorId()).isEqualTo(authorId);
		assertThat(found.getMenuId()).isEqualTo(menuId);
		assertThat(found.getTitle()).isEqualTo(RecipeFixture.TITLE);
		assertThat(found.getOptions()).extracting(RecipeOption::getName).containsExactly("C", "A", "B");
		assertThat(found.getOptions()).extracting(RecipeOption::getPosition).containsExactly(0, 1, 2);
		assertThat(found.getOptions()).allSatisfy(option -> {
			assertThat(option.getId()).isNotNull();
			assertThat(option.getCreatedAt()).isNotNull();
		});
	}

	@Test
	@DisplayName("옵션 없이 레시피를 저장하고 다시 조회하면 빈 옵션 목록을 가진다")
	void saveWithEmptyOptions() {
		// given
		Recipe saved = recipeRepository.saveAndFlush(RecipeFixture.recipe(authorId, menuId, List.of()));
		entityManager.clear();

		// when
		Recipe found = recipeRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getOptions()).isEmpty();
	}

	@Test
	@DisplayName("레시피를 저장하면 옵션이 recipe_options 테이블에 레시피 ID와 함께 저장된다")
	void optionsStoredWithRecipeId() {
		// given
		Recipe saved = recipeRepository.saveAndFlush(RecipeFixture.recipe(authorId, menuId, RecipeFixture.OPTION_NAMES));

		// when
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM recipe_options WHERE recipe_id = ?", Integer.class, saved.getId());

		// then
		assertThat(count).isEqualTo(RecipeFixture.OPTION_NAMES.size());
	}

	@Test
	@DisplayName("recipes 행을 DB에서 삭제하면 recipe_options 행도 함께 삭제된다")
	void optionsDeletedOnRecipeDeleteCascade() {
		// given
		Recipe saved = recipeRepository.saveAndFlush(RecipeFixture.recipe(authorId, menuId, RecipeFixture.OPTION_NAMES));
		entityManager.clear();

		// when
		jdbcTemplate.update("DELETE FROM recipes WHERE id = ?", saved.getId());

		// then
		Integer count = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM recipe_options WHERE recipe_id = ?", Integer.class, saved.getId());
		assertThat(count).isZero();
	}

	@Test
	@DisplayName("존재하지 않는 작성자 ID로 레시피를 저장하면 FK 제약 위반이 발생한다")
	void authorForeignKey() {
		// given
		Recipe recipe = RecipeFixture.recipe(MISSING_ID, menuId, RecipeFixture.OPTION_NAMES);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> recipeRepository.saveAndFlush(recipe));
	}

	@Test
	@DisplayName("존재하지 않는 메뉴 ID로 레시피를 저장하면 FK 제약 위반이 발생한다")
	void menuForeignKey() {
		// given
		Recipe recipe = RecipeFixture.recipe(authorId, MISSING_ID, RecipeFixture.OPTION_NAMES);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> recipeRepository.saveAndFlush(recipe));
	}
}
