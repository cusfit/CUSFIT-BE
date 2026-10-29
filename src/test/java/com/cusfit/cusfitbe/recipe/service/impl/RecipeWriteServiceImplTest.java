package com.cusfit.cusfitbe.recipe.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.entity.Recipe;
import com.cusfit.cusfitbe.recipe.entity.RecipeOption;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.recipe.repository.RecipeRepository;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.service.UserReadService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeWriteServiceImplTest {

	@Mock
	private RecipeRepository recipeRepository;

	@Mock
	private UserReadService userReadService;

	@Mock
	private MenuReadService menuReadService;

	@InjectMocks
	private RecipeWriteServiceImpl recipeWriteService;

	@Test
	@DisplayName("존재하는 작성자와 메뉴로 생성하면 옵션 순서를 유지해 저장하고 레시피 응답을 반환한다")
	void create() {
		// given
		CreateRecipeRequest request = RecipeFixture.createRequest();
		givenAuthorExists();
		givenMenuExists();
		given(recipeRepository.save(any(Recipe.class)))
				.willAnswer(invocation -> RecipeFixture.persisted(invocation.getArgument(0), RecipeFixture.RECIPE_ID));

		// when
		RecipeResponse response = recipeWriteService.create(request);

		// then
		ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
		then(recipeRepository).should().save(captor.capture());
		Recipe saved = captor.getValue();
		assertThat(saved.getAuthorId()).isEqualTo(RecipeFixture.AUTHOR_ID);
		assertThat(saved.getMenuId()).isEqualTo(RecipeFixture.MENU_ID);
		assertThat(saved.getTitle()).isEqualTo(RecipeFixture.TITLE);
		assertThat(saved.getOptions()).extracting(RecipeOption::getName)
				.containsExactlyElementsOf(RecipeFixture.OPTION_NAMES);
		assertThat(response).isEqualTo(RecipeFixture.recipeResponse());
	}

	@Test
	@DisplayName("옵션 없이 생성하면 빈 옵션 목록을 가진 레시피 응답을 반환한다")
	void createWithEmptyOptions() {
		// given
		CreateRecipeRequest request = RecipeFixture.createRequest(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID,
				List.of());
		givenAuthorExists();
		givenMenuExists();
		given(recipeRepository.save(any(Recipe.class)))
				.willAnswer(invocation -> RecipeFixture.persisted(invocation.getArgument(0), RecipeFixture.RECIPE_ID));

		// when
		RecipeResponse response = recipeWriteService.create(request);

		// then
		assertThat(response.id()).isEqualTo(RecipeFixture.RECIPE_ID);
		assertThat(response.options()).isEmpty();
	}

	@Test
	@DisplayName("없는 작성자로 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createWithMissingAuthor() {
		// given
		CreateRecipeRequest request = RecipeFixture.createRequest();
		given(userReadService.getById(RecipeFixture.AUTHOR_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));
		lenient().when(menuReadService.getById(RecipeFixture.MENU_ID)).thenReturn(CatalogFixture.menuResponse());

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> recipeWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		then(recipeRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("없는 메뉴로 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createWithMissingMenu() {
		// given
		CreateRecipeRequest request = RecipeFixture.createRequest();
		lenient().when(userReadService.getById(RecipeFixture.AUTHOR_ID)).thenReturn(authorResponse());
		given(menuReadService.getById(RecipeFixture.MENU_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> recipeWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		then(recipeRepository).should(never()).save(any());
	}

	private void givenAuthorExists() {
		given(userReadService.getById(RecipeFixture.AUTHOR_ID)).willReturn(authorResponse());
	}

	private void givenMenuExists() {
		given(menuReadService.getById(RecipeFixture.MENU_ID)).willReturn(CatalogFixture.menuResponse());
	}

	private static UserResponse authorResponse() {
		return UserResponse.from(UserFixture.persistedUser());
	}
}
