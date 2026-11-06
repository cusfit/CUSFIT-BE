package com.cusfit.cusfitbe.support.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.request.SubmitInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.NoticeSummaryResponse;
import com.cusfit.cusfitbe.support.entity.InquiryType;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.support.service.FaqReadService;
import com.cusfit.cusfitbe.support.service.InquiryWriteService;
import com.cusfit.cusfitbe.support.service.NoticeReadService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = SupportApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class SupportApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NoticeReadService noticeReadService;

	@MockitoBean
	private FaqReadService faqReadService;

	@MockitoBean
	private InquiryWriteService inquiryWriteService;

	@Test
	@DisplayName("공지 목록을 토큰 없이 조회하면 200과 공지 목록을 반환한다")
	void getNotices() throws Exception {
		// given
		given(noticeReadService.getAll()).willReturn(List.of(new NoticeSummaryResponse(SupportFixture.NOTICE_ID,
				SupportFixture.NOTICE_TITLE, SupportFixture.CREATED_AT)));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notices"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].title").value(SupportFixture.NOTICE_TITLE))
				.andExpect(jsonPath("$.data[0].content").doesNotExist());
	}

	@Test
	@DisplayName("공지 상세를 조회하면 본문을 포함해 반환한다")
	void getNotice() throws Exception {
		// given
		given(noticeReadService.getById(SupportFixture.NOTICE_ID)).willReturn(SupportFixture.noticeResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notices/{noticeId}", SupportFixture.NOTICE_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").value(SupportFixture.NOTICE_CONTENT));
	}

	@Test
	@DisplayName("keyword로 FAQ를 검색하면 keyword를 서비스에 넘기고 FAQ 목록을 반환한다")
	void searchFaqs() throws Exception {
		// given
		given(faqReadService.search("레시피")).willReturn(List.of(SupportFixture.faqResponse()));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/faqs").param("keyword", "레시피"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].question").value(SupportFixture.FAQ_QUESTION));
	}

	@Test
	@DisplayName("1:1 문의를 보내면 토큰 subject와 유형·내용·첨부를 서비스에 넘긴다")
	void submitInquiry() throws Exception {
		// given
		MockMultipartFile image = new MockMultipartFile("images", "error.png", "image/png", new byte[] {1});

		// when
		ResultActions result = mockMvc.perform(multipart("/api/v1/inquiries")
				.file(image)
				.param("type", "BUG_REPORT")
				.param("content", SupportFixture.INQUIRY_CONTENT)
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isCreated());
		ArgumentCaptor<SubmitInquiryRequest> captor = ArgumentCaptor.forClass(SubmitInquiryRequest.class);
		verify(inquiryWriteService).submit(eq(SUBJECT), captor.capture());
		assertThat(captor.getValue().type()).isEqualTo(InquiryType.BUG_REPORT);
		assertThat(captor.getValue().content()).isEqualTo(SupportFixture.INQUIRY_CONTENT);
		assertThat(captor.getValue().images()).hasSize(1);
	}

	@Test
	@DisplayName("문의 유형 없이 1:1 문의를 보내면 400 INVALID_REQUEST를 반환하고 접수하지 않는다")
	void submitInquiryWithoutType() throws Exception {
		// when
		ResultActions result = mockMvc.perform(multipart("/api/v1/inquiries")
				.param("content", SupportFixture.INQUIRY_CONTENT)
				.with(jwt()));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
		verify(inquiryWriteService, never()).submit(anyString(), any());
	}

	@Test
	@DisplayName("아직 구현되지 않은 1:1 문의는 501 NOT_IMPLEMENTED를 반환한다")
	void submitInquiryNotImplemented() throws Exception {
		// given
		willThrow(new BusinessException(ErrorCode.NOT_IMPLEMENTED)).given(inquiryWriteService)
				.submit(anyString(), any());

		// when
		ResultActions result = mockMvc.perform(multipart("/api/v1/inquiries")
				.param("type", "ETC")
				.param("content", SupportFixture.INQUIRY_CONTENT)
				.with(jwt()));

		// then
		result.andExpect(status().isNotImplemented())
				.andExpect(jsonPath("$.error.code").value("NOT_IMPLEMENTED"));
	}
}
