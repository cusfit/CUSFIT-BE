package com.cusfit.cusfitbe.recipe.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.request.AiAnalysisRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeOptionRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeSearchRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeSort;
import com.cusfit.cusfitbe.recipe.dto.request.RegisterRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeLikeResponse;
import com.cusfit.cusfitbe.recipe.entity.OptionGroup;
import com.cusfit.cusfitbe.recipe.service.RecipeAnalysisService;
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

@WebMvcTest(controllers = RecipeApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class RecipeApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";
	private static final Long RECIPE_ID = 30L;
	private static final Long MENU_ID = 10L;
	private static final String OPTION_JSON = "{\"name\": \"헤이즐넛 시럽\", \"group\": \"MIDDLE\", \"quantity\": 2}";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RecipeReadService recipeReadService;

	@MockitoBean
	private RecipeWriteService recipeWriteService;

	@MockitoBean
	private RecipeAnalysisService recipeAnalysisService;

	@Test
	@DisplayName("자연어로 AI 분석을 요청하면 토큰 subject와 입력을 분석 서비스에 넘긴다")
	void analyzeWithText() throws Exception {
		// when
		ResultActions result = mockMvc.perform(multipart("/api/v1/recipes/ai-analysis")
				.param("text", "자바칩 프라푸치노에 휘핑 많이")
				.param("brandId", "1")
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk());
		ArgumentCaptor<AiAnalysisRequest> captor = ArgumentCaptor.forClass(AiAnalysisRequest.class);
		verify(recipeAnalysisService).analyze(eq(SUBJECT), captor.capture());
		assertThat(captor.getValue().text()).isEqualTo("자바칩 프라푸치노에 휘핑 많이");
		assertThat(captor.getValue().brandId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("이미지와 자연어 없이 AI 분석을 요청하면 400 INVALID_REQUEST를 반환한다")
	void analyzeWithoutInput() throws Exception {
		// when
		ResultActions result = mockMvc.perform(multipart("/api/v1/recipes/ai-analysis").param("brandId", "1")
				.with(jwt()));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
		verify(recipeAnalysisService, never()).analyze(anyString(), any());
	}

	@Test
	@DisplayName("레시피를 저장하면 201을 반환하고 토큰 subject와 그룹·수량을 포함한 옵션을 넘긴다")
	void register() throws Exception {
		// when
		ResultActions result = postRecipe(registerJson("자바칩 프라푸치노 커스텀", "[" + OPTION_JSON + "]"));

		// then
		result.andExpect(status().isCreated());
		RecipeOptionRequest option = new RecipeOptionRequest("헤이즐넛 시럽", OptionGroup.MIDDLE, null, 2);
		verify(recipeWriteService).register(SUBJECT,
				new RegisterRecipeRequest(MENU_ID, "자바칩 프라푸치노 커스텀", List.of(option), null));
	}

	@Test
	@DisplayName("옵션 없이 레시피를 저장하면 400 INVALID_REQUEST를 반환한다")
	void registerWithoutOptions() throws Exception {
		// when
		ResultActions result = postRecipe(registerJson("레시피", "[]"));

		// then
		expectRejectedRegister(result);
	}

	@Test
	@DisplayName("레시피 이름이 200자를 넘으면 400 INVALID_REQUEST를 반환한다")
	void registerWithTooLongTitle() throws Exception {
		// when
		ResultActions result = postRecipe(registerJson("가".repeat(201), "[" + OPTION_JSON + "]"));

		// then
		expectRejectedRegister(result);
	}

	@Test
	@DisplayName("옵션 그룹이 없으면 400 INVALID_REQUEST를 반환한다")
	void registerWithoutOptionGroup() throws Exception {
		// when
		ResultActions result = postRecipe(registerJson("레시피", "[{\"name\": \"샷 추가\"}]"));

		// then
		expectRejectedRegister(result);
	}

	@Test
	@DisplayName("비로그인으로 레시피 목록을 조회하면 subject 없이 인기순, 0페이지, 크기 20으로 서비스에 넘긴다")
	void searchAnonymouslyWithDefaults() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/recipes").param("keyword", "자바칩"));

		// then
		result.andExpect(status().isOk());
		verify(recipeReadService).search(isNull(),
				eq(new RecipeSearchRequest("자바칩", null, RecipeSort.POPULAR, 0, null, 20)));
	}

	@Test
	@DisplayName("레시피 목록 크기가 50을 넘으면 400 INVALID_REQUEST를 반환한다")
	void searchWithTooLargeSize() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/recipes").param("size", "51"));

		// then
		result.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("로그인한 사용자가 레시피 상세를 조회하면 토큰 subject를 넘긴다")
	void getDetailAsMember() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/recipes/{recipeId}", RECIPE_ID)
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk());
		verify(recipeReadService).getDetail(SUBJECT, RECIPE_ID);
	}

	@Test
	@DisplayName("아직 구현되지 않은 레시피 상세는 501 NOT_IMPLEMENTED를 반환한다")
	void getDetailNotImplemented() throws Exception {
		// given
		given(recipeReadService.getDetail(null, RECIPE_ID)).willThrow(new BusinessException(ErrorCode.NOT_IMPLEMENTED));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/recipes/{recipeId}", RECIPE_ID));

		// then
		result.andExpect(status().isNotImplemented())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("NOT_IMPLEMENTED"));
	}

	@Test
	@DisplayName("좋아요를 누르면 200과 좋아요 상태·개수를 반환한다")
	void like() throws Exception {
		// given
		given(recipeWriteService.like(SUBJECT, RECIPE_ID)).willReturn(new RecipeLikeResponse(true, 13));

		// when
		ResultActions result = mockMvc.perform(put("/api/v1/recipes/{recipeId}/like", RECIPE_ID)
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.liked").value(true))
				.andExpect(jsonPath("$.data.likeCount").value(13));
	}

	@Test
	@DisplayName("레시피를 삭제하면 204를 반환한다")
	void deleteRecipe() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/recipes/{recipeId}", RECIPE_ID)
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isNoContent());
		verify(recipeWriteService).delete(SUBJECT, RECIPE_ID);
	}

	@Test
	@DisplayName("AI 피드백 평가가 없으면 400 INVALID_REQUEST를 반환한다")
	void submitAiFeedbackWithoutRating() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post("/api/v1/recipes/{recipeId}/ai-feedback", RECIPE_ID)
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reasons\": [\"WRONG_MENU\"]}"));

		// then
		result.andExpect(status().isBadRequest());
		verify(recipeWriteService, never()).submitAiFeedback(anyString(), any(), any());
	}

	private ResultActions postRecipe(String body) throws Exception {
		return mockMvc.perform(post("/api/v1/recipes")
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private static String registerJson(String title, String optionsJson) {
		return "{\"menuId\": " + MENU_ID + ", \"title\": \"" + title + "\", \"options\": " + optionsJson + "}";
	}

	private void expectRejectedRegister(ResultActions result) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
		verify(recipeWriteService, never()).register(anyString(), any());
	}
}
