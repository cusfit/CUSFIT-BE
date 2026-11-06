package com.cusfit.cusfitbe.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.report.dto.request.ReportRecipeRequest;
import com.cusfit.cusfitbe.report.entity.ReportReason;
import com.cusfit.cusfitbe.report.service.ReportWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = ReportApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class ReportApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";
	private static final Long RECIPE_ID = 30L;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReportWriteService reportWriteService;

	@Test
	@DisplayName("레시피를 신고하면 201을 반환하고 토큰 subject, 레시피 ID, 사유를 서비스에 넘긴다")
	void reportRecipe() throws Exception {
		// when
		ResultActions result = postReport("{\"reason\": \"AD\", \"content\": \"광고 글입니다\"}");

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));
		verify(reportWriteService).reportRecipe(SUBJECT, RECIPE_ID, new ReportRecipeRequest(ReportReason.AD, "광고 글입니다"));
	}

	@Test
	@DisplayName("정의되지 않은 사유로 신고하면 400 INVALID_REQUEST를 반환하고 신고하지 않는다")
	void reportWithUnknownReason() throws Exception {
		// when
		ResultActions result = postReport("{\"reason\": \"BORING\"}");

		// then
		result.andExpect(status().isBadRequest());
		verify(reportWriteService, never()).reportRecipe(anyString(), anyLong(), any());
	}

	@Test
	@DisplayName("상세 내용이 200자를 넘으면 400 INVALID_REQUEST를 반환하고 신고하지 않는다")
	void reportWithTooLongContent() throws Exception {
		// when
		ResultActions result = postReport("{\"reason\": \"ETC\", \"content\": \"" + "가".repeat(201) + "\"}");

		// then
		result.andExpect(status().isBadRequest());
		verify(reportWriteService, never()).reportRecipe(anyString(), anyLong(), any());
	}

	private ResultActions postReport(String body) throws Exception {
		return mockMvc.perform(post("/api/v1/recipes/{recipeId}/reports", RECIPE_ID)
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}
}
