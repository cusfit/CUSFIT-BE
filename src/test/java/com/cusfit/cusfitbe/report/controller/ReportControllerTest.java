package com.cusfit.cusfitbe.report.controller;

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
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.fixture.ReportFixture;
import com.cusfit.cusfitbe.report.service.ReportReadService;
import com.cusfit.cusfitbe.report.service.ReportWriteService;
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

@WebMvcTest(controllers = ReportController.class)
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class ReportControllerTest {

	private static final String BASE_PATH = "/api/dev/reports";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReportReadService reportReadService;

	@MockitoBean
	private ReportWriteService reportWriteService;

	@Test
	@DisplayName("신고를 생성하면 201과 Location, 신고 응답을 반환한다")
	void create() throws Exception {
		// given
		given(reportWriteService.create(any(CreateReportRequest.class))).willReturn(ReportFixture.reportResponse());

		// when
		ResultActions result = postJson(ReportFixture.createRequestJson());

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith(BASE_PATH + "/" + ReportFixture.REPORT_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(ReportFixture.REPORT_ID))
				.andExpect(jsonPath("$.data.reporterId").value(ReportFixture.REPORTER_ID))
				.andExpect(jsonPath("$.data.recipeId").value(ReportFixture.RECIPE_ID))
				.andExpect(jsonPath("$.data.content").value(ReportFixture.CONTENT))
				.andExpect(jsonPath("$.data.createdAt").exists());
		ArgumentCaptor<CreateReportRequest> captor = ArgumentCaptor.forClass(CreateReportRequest.class);
		verify(reportWriteService).create(captor.capture());
		assertThat(captor.getValue()).isEqualTo(ReportFixture.createRequest());
	}

	@Test
	@DisplayName("신고자 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithoutReporterId() throws Exception {
		// given
		String body = ReportFixture.createRequestJson(null, ReportFixture.RECIPE_ID, ReportFixture.CONTENT);

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("레시피 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithoutRecipeId() throws Exception {
		// given
		String body = ReportFixture.createRequestJson(ReportFixture.REPORTER_ID, null, ReportFixture.CONTENT);

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("내용이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithBlankContent() throws Exception {
		// given
		String body = ReportFixture.createRequestJson(ReportFixture.REPORTER_ID, ReportFixture.RECIPE_ID, " ");

		// when & then
		expectInvalidRequest(postJson(body));
	}

	@Test
	@DisplayName("없는 신고자나 레시피로 신고를 생성하면 404 NOT_FOUND를 반환한다")
	void createWithMissingReference() throws Exception {
		// given
		given(reportWriteService.create(any(CreateReportRequest.class)))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = postJson(ReportFixture.createRequestJson());

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("신고를 단건 조회하면 200과 신고 응답을 반환한다")
	void getById() throws Exception {
		// given
		given(reportReadService.getById(ReportFixture.REPORT_ID)).willReturn(ReportFixture.reportResponse());

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", ReportFixture.REPORT_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(ReportFixture.REPORT_ID))
				.andExpect(jsonPath("$.data.reporterId").value(ReportFixture.REPORTER_ID))
				.andExpect(jsonPath("$.data.recipeId").value(ReportFixture.RECIPE_ID))
				.andExpect(jsonPath("$.data.content").value(ReportFixture.CONTENT));
	}

	@Test
	@DisplayName("없는 신고를 조회하면 404 NOT_FOUND를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		given(reportReadService.getById(ReportFixture.REPORT_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", ReportFixture.REPORT_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("숫자가 아닌 신고 ID로 조회하면 400 INVALID_REQUEST를 반환한다")
	void getByIdWithInvalidIdType() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", "abc"));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
	}

	private ResultActions postJson(String body) throws Exception {
		return mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private void expectInvalidRequest(ResultActions result) throws Exception {
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(reportWriteService, never()).create(any());
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
