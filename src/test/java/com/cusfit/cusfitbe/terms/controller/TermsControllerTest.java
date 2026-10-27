package com.cusfit.cusfitbe.terms.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.SecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.fixture.TermsFixture;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import com.cusfit.cusfitbe.terms.service.TermsWriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(controllers = TermsController.class)
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class TermsControllerTest {

	private static final String BASE_PATH = "/api/dev/terms";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TermsReadService termsReadService;

	@MockitoBean
	private TermsWriteService termsWriteService;

	@Test
	@DisplayName("약관 생성 요청을 보내면 201과 Location, 생성된 약관을 반환한다")
	void create() throws Exception {
		// given
		CreateTermsRequest request = TermsFixture.createRequest();
		given(termsWriteService.create(request)).willReturn(TermsFixture.response(1L));

		// when
		ResultActions result = postJson(request);

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith(BASE_PATH + "/1")))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.type").value(TermsFixture.TYPE))
				.andExpect(jsonPath("$.data.version").value(TermsFixture.VERSION))
				.andExpect(jsonPath("$.data.contentUrl").value(TermsFixture.CONTENT_URL))
				.andExpect(jsonPath("$.data.effectiveAt").value(TermsFixture.EFFECTIVE_AT.toString()))
				.andExpect(jsonPath("$.data.createdAt").value(TermsFixture.CREATED_AT.toString()));
	}

	@Test
	@DisplayName("type이 공백인 생성 요청을 보내면 400 INVALID_REQUEST를 반환하고 서비스를 호출하지 않는다")
	void createWithBlankType() throws Exception {
		// given
		CreateTermsRequest request = TermsFixture.createRequestWithType(" ");

		// when
		ResultActions result = postJson(request);

		// then
		expectError(result, ErrorCode.INVALID_REQUEST);
		result.andExpect(status().isBadRequest());
		then(termsWriteService).should(never()).create(any());
	}

	@Test
	@DisplayName("effectiveAt이 없는 생성 요청을 보내면 400 INVALID_REQUEST를 반환한다")
	void createWithoutEffectiveAt() throws Exception {
		// given
		CreateTermsRequest request = TermsFixture.createRequestWithEffectiveAt(null);

		// when
		ResultActions result = postJson(request);

		// then
		result.andExpect(status().isBadRequest());
		expectError(result, ErrorCode.INVALID_REQUEST);
		then(termsWriteService).should(never()).create(any());
	}

	@Test
	@DisplayName("존재하는 약관 ID로 조회하면 200과 약관을 반환한다")
	void getById() throws Exception {
		// given
		given(termsReadService.getById(1L)).willReturn(TermsFixture.response(1L));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/1"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.type").value(TermsFixture.TYPE));
	}

	@Test
	@DisplayName("존재하지 않는 약관 ID로 조회하면 404 NOT_FOUND를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		given(termsReadService.getById(99L)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/99"));

		// then
		result.andExpect(status().isNotFound());
		expectError(result, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("숫자가 아닌 약관 ID로 조회하면 400 INVALID_REQUEST를 반환한다")
	void getByInvalidId() throws Exception {
		// given
		String path = BASE_PATH + "/not-a-number";

		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(status().isBadRequest());
		expectError(result, ErrorCode.INVALID_REQUEST);
	}

	private ResultActions postJson(CreateTermsRequest request) throws Exception {
		return mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));
	}

	private static void expectError(ResultActions result, ErrorCode errorCode) throws Exception {
		result.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(errorCode.name()));
	}
}
