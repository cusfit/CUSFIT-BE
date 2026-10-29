package com.cusfit.cusfitbe.recipe.fixture;

import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeOptionRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeOptionResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.entity.Recipe;
import com.cusfit.cusfitbe.recipe.entity.RecipeOption;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.test.util.ReflectionTestUtils;

public final class RecipeFixture {

	public static final Long RECIPE_ID = 100L;
	public static final Long AUTHOR_ID = UserFixture.ID;
	public static final Long MENU_ID = CatalogFixture.MENU_ID;
	public static final String TITLE = "덜 달게 마시는 아메리카노";
	public static final String FIRST_OPTION_NAME = "샷 추가";
	public static final String SECOND_OPTION_NAME = "시럽 1펌프";
	public static final List<String> OPTION_NAMES = List.of(FIRST_OPTION_NAME, SECOND_OPTION_NAME);
	public static final Long FIRST_OPTION_ID = 1000L;
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private RecipeFixture() {
	}

	public static Recipe recipe() {
		return recipe(AUTHOR_ID, MENU_ID, OPTION_NAMES);
	}

	public static Recipe recipe(Long authorId, Long menuId, List<String> optionNames) {
		return Recipe.create(authorId, menuId, TITLE, optionNames);
	}

	/** 저장된 상태를 흉내 낸다. Recipe와 각 RecipeOption에 id/createdAt을 채운다. 서비스 단위 테스트 전용. */
	public static Recipe persisted(Recipe recipe, Long id) {
		ReflectionTestUtils.setField(recipe, "id", id);
		ReflectionTestUtils.setField(recipe, "createdAt", CREATED_AT);
		List<RecipeOption> options = recipe.getOptions();
		for (int i = 0; i < options.size(); i++) {
			ReflectionTestUtils.setField(options.get(i), "id", FIRST_OPTION_ID + i);
			ReflectionTestUtils.setField(options.get(i), "createdAt", CREATED_AT);
		}
		return recipe;
	}

	public static Recipe persistedRecipe() {
		return persisted(recipe(), RECIPE_ID);
	}

	public static CreateRecipeRequest createRequest() {
		return createRequest(AUTHOR_ID, MENU_ID, OPTION_NAMES);
	}

	public static CreateRecipeRequest createRequest(Long authorId, Long menuId, List<String> optionNames) {
		List<CreateRecipeOptionRequest> options = optionNames.stream()
				.map(CreateRecipeOptionRequest::new)
				.toList();
		return new CreateRecipeRequest(authorId, menuId, TITLE, options);
	}

	public static RecipeResponse recipeResponse() {
		List<RecipeOptionResponse> options = new ArrayList<>();
		for (int i = 0; i < OPTION_NAMES.size(); i++) {
			options.add(new RecipeOptionResponse(FIRST_OPTION_ID + i, OPTION_NAMES.get(i), i, CREATED_AT));
		}
		return new RecipeResponse(RECIPE_ID, AUTHOR_ID, MENU_ID, TITLE, options, CREATED_AT);
	}

	public static String createRequestJson(Long authorId, Long menuId, String title, String optionsJson) {
		return "{\"authorId\": " + authorId
				+ ", \"menuId\": " + menuId
				+ ", \"title\": " + quote(title)
				+ ", \"options\": " + optionsJson + "}";
	}

	public static String createRequestJson() {
		return createRequestJson(AUTHOR_ID, MENU_ID, TITLE, optionsJson(OPTION_NAMES));
	}

	public static String optionsJson(List<String> names) {
		return names.stream()
				.map(name -> "{\"name\": " + quote(name) + "}")
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}
