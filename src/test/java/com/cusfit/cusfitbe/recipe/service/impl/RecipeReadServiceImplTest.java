package com.cusfit.cusfitbe.recipe.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.recipe.repository.RecipeRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeReadServiceImplTest {

	@Mock
	private RecipeRepository recipeRepository;

	@InjectMocks
	private RecipeReadServiceImpl recipeReadService;

	@Test
	@DisplayName("존재하는 레시피 ID로 조회하면 옵션을 포함한 레시피 응답을 반환한다")
	void getById() {
		// given
		given(recipeRepository.findById(RecipeFixture.RECIPE_ID))
				.willReturn(Optional.of(RecipeFixture.persistedRecipe()));

		// when
		RecipeResponse response = recipeReadService.getById(RecipeFixture.RECIPE_ID);

		// then
		assertThat(response).isEqualTo(RecipeFixture.recipeResponse());
	}

	@Test
	@DisplayName("없는 레시피 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getByIdNotFound() {
		// given
		given(recipeRepository.findById(RecipeFixture.RECIPE_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> recipeReadService.getById(RecipeFixture.RECIPE_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
