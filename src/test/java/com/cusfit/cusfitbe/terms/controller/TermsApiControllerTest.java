package com.cusfit.cusfitbe.terms.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.fixture.TermsFixture;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = TermsApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class TermsApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TermsReadService termsReadService;

	@Test
	@DisplayName("약관 목록을 토큰 없이 조회하면 200과 시행 중인 약관 목록을 반환한다")
	void getCurrentTerms() throws Exception {
		// given
		given(termsReadService.getCurrentTerms()).willReturn(List.of(TermsFixture.response(1L)));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/terms"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].type").value(TermsFixture.TYPE))
				.andExpect(jsonPath("$.data[0].contentUrl").value(TermsFixture.CONTENT_URL));
	}

	@Test
	@DisplayName("없는 약관을 조회하면 404 NOT_FOUND를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		given(termsReadService.getById(1L)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/terms/{termsId}", 1L));

		// then
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
	}
}
