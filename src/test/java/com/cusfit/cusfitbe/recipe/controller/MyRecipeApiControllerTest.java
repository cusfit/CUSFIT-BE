package com.cusfit.cusfitbe.recipe.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.recipe.dto.request.MyRecipePageRequest;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = MyRecipeApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class MyRecipeApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RecipeReadService recipeReadService;

	@Test
	@DisplayName("내 레시피를 브랜드로 걸러 조회하면 토큰 subject와 기본 크기 20을 서비스에 넘긴다")
	void getMyRecipes() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/users/me/recipes")
				.param("brandId", "1")
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk());
		verify(recipeReadService).getMyRecipes(SUBJECT, new MyRecipePageRequest(1L, null, 20));
	}

	@Test
	@DisplayName("저장한 레시피를 커서로 조회하면 커서를 서비스에 넘긴다")
	void getSavedRecipes() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/users/me/saved-recipes")
				.param("cursor", "abc")
				.param("size", "10")
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk());
		verify(recipeReadService).getSavedRecipes(SUBJECT, new MyRecipePageRequest(null, "abc", 10));
	}
}
