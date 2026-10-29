package com.cusfit.cusfitbe.recipe.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RecipeTest {

	@Test
	@DisplayName("유효한 값으로 생성하면 작성자, 메뉴, 제목과 입력 순서대로 위치가 매겨진 옵션을 가진다")
	void create() {
		// given
		List<String> optionNames = List.of("C", "A", "B");

		// when
		Recipe recipe = Recipe.create(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, RecipeFixture.TITLE, optionNames);

		// then
		assertThat(recipe.getId()).isNull();
		assertThat(recipe.getAuthorId()).isEqualTo(RecipeFixture.AUTHOR_ID);
		assertThat(recipe.getMenuId()).isEqualTo(RecipeFixture.MENU_ID);
		assertThat(recipe.getTitle()).isEqualTo(RecipeFixture.TITLE);
		assertThat(recipe.getOptions()).extracting(RecipeOption::getName).containsExactly("C", "A", "B");
		assertThat(recipe.getOptions()).extracting(RecipeOption::getPosition).containsExactly(0, 1, 2);
	}

	@Test
	@DisplayName("옵션 목록이 비어 있으면 옵션 없는 레시피가 생성된다")
	void createWithEmptyOptions() {
		// given
		List<String> optionNames = List.of();

		// when
		Recipe recipe = RecipeFixture.recipe(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, optionNames);

		// then
		assertThat(recipe.getOptions()).isEmpty();
	}

	@Test
	@DisplayName("옵션 목록을 외부에서 변경하려 하면 UnsupportedOperationException이 발생한다")
	void optionsAreUnmodifiable() {
		// given
		Recipe recipe = RecipeFixture.recipe();

		// when & then
		assertThrows(UnsupportedOperationException.class, () -> recipe.getOptions().clear());
	}

	@Test
	@DisplayName("작성자 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullAuthorId() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> RecipeFixture.recipe(null, RecipeFixture.MENU_ID, RecipeFixture.OPTION_NAMES));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("메뉴 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullMenuId() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> RecipeFixture.recipe(RecipeFixture.AUTHOR_ID, null, RecipeFixture.OPTION_NAMES));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("제목이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankTitle(String title) {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Recipe.create(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, title, RecipeFixture.OPTION_NAMES));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("옵션 목록이 null이면 INVALID_REQUEST 예외가 발생한다")
	void nullOptionNames() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> RecipeFixture.recipe(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, null));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" "})
	@DisplayName("옵션 이름이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankOptionName(String optionName) {
		// given
		List<String> optionNames = Arrays.asList(RecipeFixture.FIRST_OPTION_NAME, optionName);

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> RecipeFixture.recipe(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, optionNames));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
