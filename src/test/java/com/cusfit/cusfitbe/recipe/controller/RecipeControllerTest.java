package com.cusfit.cusfitbe.recipe.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.SecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeOptionRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import com.cusfit.cusfitbe.recipe.service.RecipeWriteService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = RecipeController.class)
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class RecipeControllerTest {

	private static final String BASE_PATH = "/api/dev/recipes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RecipeReadService recipeReadService;

	@MockitoBean
	private RecipeWriteService recipeWriteService;

	@Test
	@DisplayName("레시피를 생성하면 201과 Location, 옵션을 포함한 레시피를 반환한다")
	void create() throws Exception {
		// given
		given(recipeWriteService.create(any(CreateRecipeRequest.class))).willReturn(RecipeFixture.recipeResponse());

		// when
		ResultActions result = postJson(RecipeFixture.createRequestJson());

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith(BASE_PATH + "/" + RecipeFixture.RECIPE_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(RecipeFixture.RECIPE_ID))
				.andExpect(jsonPath("$.data.authorId").value(RecipeFixture.AUTHOR_ID))
				.andExpect(jsonPath("$.data.menuId").value(RecipeFixture.MENU_ID))
				.andExpect(jsonPath("$.data.title").value(RecipeFixture.TITLE))
				.andExpect(jsonPath("$.data.options[0].id").value(RecipeFixture.FIRST_OPTION_ID))
				.andExpect(jsonPath("$.data.options[0].name").value(RecipeFixture.FIRST_OPTION_NAME))
				.andExpect(jsonPath("$.data.options[0].position").value(0))
				.andExpect(jsonPath("$.data.options[1].name").value(RecipeFixture.SECOND_OPTION_NAME))
				.andExpect(jsonPath("$.data.options[1].position").value(1))
				.andExpect(jsonPath("$.data.createdAt").exists());
		ArgumentCaptor<CreateRecipeRequest> captor = ArgumentCaptor.forClass(CreateRecipeRequest.class);
		verify(recipeWriteService).create(captor.capture());
		assertThat(captor.getValue()).isEqualTo(RecipeFixture.createRequest());
	}

	@Test
	@DisplayName("빈 옵션 목록으로 레시피를 생성하면 201을 반환한다")
	void createWithEmptyOptions() throws Exception {
		// given
		RecipeResponse response = new RecipeResponse(RecipeFixture.RECIPE_ID, RecipeFixture.AUTHOR_ID,
				RecipeFixture.MENU_ID, RecipeFixture.TITLE, List.of(), RecipeFixture.CREATED_AT);
		given(recipeWriteService.create(any(CreateRecipeRequest.class))).willReturn(response);
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID,
				RecipeFixture.TITLE, "[]");

		// when
		ResultActions result = postJson(body);

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.options").isEmpty());
		ArgumentCaptor<CreateRecipeRequest> captor = ArgumentCaptor.forClass(CreateRecipeRequest.class);
		verify(recipeWriteService).create(captor.capture());
		assertThat(captor.getValue().options()).isEqualTo(List.<CreateRecipeOptionRequest>of());
	}

	@Test
	@DisplayName("작성자 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithoutAuthorId() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(null, RecipeFixture.MENU_ID, RecipeFixture.TITLE,
				RecipeFixture.optionsJson(RecipeFixture.OPTION_NAMES));

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("메뉴 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithoutMenuId() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, null, RecipeFixture.TITLE,
				RecipeFixture.optionsJson(RecipeFixture.OPTION_NAMES));

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("제목이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithBlankTitle() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID, " ",
				RecipeFixture.optionsJson(RecipeFixture.OPTION_NAMES));

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("옵션 목록이 null이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithNullOptions() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID,
				RecipeFixture.TITLE, "null");

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("옵션 원소가 null이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithNullOptionElement() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID,
				RecipeFixture.TITLE, "[null]");

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("옵션 이름이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithBlankOptionName() throws Exception {
		// given
		String body = RecipeFixture.createRequestJson(RecipeFixture.AUTHOR_ID, RecipeFixture.MENU_ID,
				RecipeFixture.TITLE, RecipeFixture.optionsJson(List.of(RecipeFixture.FIRST_OPTION_NAME, " ")));

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("없는 작성자나 메뉴로 레시피를 생성하면 404 NOT_FOUND를 반환한다")
	void createWithMissingReference() throws Exception {
		// given
		given(recipeWriteService.create(any(CreateRecipeRequest.class)))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = postJson(RecipeFixture.createRequestJson());

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("레시피를 단건 조회하면 200과 옵션을 포함한 레시피를 반환한다")
	void getById() throws Exception {
		// given
		given(recipeReadService.getById(RecipeFixture.RECIPE_ID)).willReturn(RecipeFixture.recipeResponse());

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", RecipeFixture.RECIPE_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(RecipeFixture.RECIPE_ID))
				.andExpect(jsonPath("$.data.title").value(RecipeFixture.TITLE))
				.andExpect(jsonPath("$.data.options.length()").value(RecipeFixture.OPTION_NAMES.size()))
				.andExpect(jsonPath("$.data.options[0].name").value(RecipeFixture.FIRST_OPTION_NAME));
	}

	@Test
	@DisplayName("없는 레시피를 조회하면 404 NOT_FOUND를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		given(recipeReadService.getById(RecipeFixture.RECIPE_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", RecipeFixture.RECIPE_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	private ResultActions postJson(String body) throws Exception {
		return mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private void expectInvalidRequest(ResultActions result) throws Exception {
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(recipeWriteService, never()).create(any());
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
