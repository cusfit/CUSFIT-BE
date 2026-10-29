package com.cusfit.cusfitbe.support.controller;

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
import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.support.service.FaqReadService;
import com.cusfit.cusfitbe.support.service.FaqWriteService;
import com.cusfit.cusfitbe.support.service.InquiryReadService;
import com.cusfit.cusfitbe.support.service.InquiryWriteService;
import com.cusfit.cusfitbe.support.service.NoticeReadService;
import com.cusfit.cusfitbe.support.service.NoticeWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = {NoticeController.class, FaqController.class, InquiryController.class})
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class SupportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NoticeReadService noticeReadService;

	@MockitoBean
	private NoticeWriteService noticeWriteService;

	@MockitoBean
	private FaqReadService faqReadService;

	@MockitoBean
	private FaqWriteService faqWriteService;

	@MockitoBean
	private InquiryReadService inquiryReadService;

	@MockitoBean
	private InquiryWriteService inquiryWriteService;

	@Test
	@DisplayName("공지를 생성하면 201과 Location, 생성된 공지를 반환한다")
	void createNotice() throws Exception {
		// given
		given(noticeWriteService.create(any(CreateNoticeRequest.class))).willReturn(SupportFixture.noticeResponse());

		// when
		ResultActions result = postJson("/api/dev/notices",
				SupportFixture.noticeJson(SupportFixture.NOTICE_TITLE, SupportFixture.NOTICE_CONTENT));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/dev/notices/" + SupportFixture.NOTICE_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(SupportFixture.NOTICE_ID))
				.andExpect(jsonPath("$.data.title").value(SupportFixture.NOTICE_TITLE))
				.andExpect(jsonPath("$.data.content").value(SupportFixture.NOTICE_CONTENT))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@ParameterizedTest
	@ValueSource(strings = {"title", "content"})
	@DisplayName("공지 제목이나 내용이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createNoticeBlankText(String blankField) throws Exception {
		// given
		String title = blankField.equals("title") ? " " : SupportFixture.NOTICE_TITLE;
		String content = blankField.equals("content") ? " " : SupportFixture.NOTICE_CONTENT;

		// when
		ResultActions result = postJson("/api/dev/notices", SupportFixture.noticeJson(title, content));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(noticeWriteService, never()).create(any());
	}

	@Test
	@DisplayName("공지를 단건 조회하면 200과 공지를 반환한다")
	void getNotice() throws Exception {
		// given
		given(noticeReadService.getById(SupportFixture.NOTICE_ID)).willReturn(SupportFixture.noticeResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/notices/{id}", SupportFixture.NOTICE_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(SupportFixture.NOTICE_ID))
				.andExpect(jsonPath("$.data.title").value(SupportFixture.NOTICE_TITLE));
	}

	@Test
	@DisplayName("없는 공지를 조회하면 404 NOT_FOUND를 반환한다")
	void getNoticeNotFound() throws Exception {
		// given
		given(noticeReadService.getById(SupportFixture.NOTICE_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/notices/{id}", SupportFixture.NOTICE_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("FAQ를 생성하면 201과 Location, 생성된 FAQ를 반환한다")
	void createFaq() throws Exception {
		// given
		given(faqWriteService.create(any(CreateFaqRequest.class))).willReturn(SupportFixture.faqResponse());

		// when
		ResultActions result = postJson("/api/dev/faqs",
				SupportFixture.faqJson(SupportFixture.FAQ_QUESTION, SupportFixture.FAQ_ANSWER));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/dev/faqs/" + SupportFixture.FAQ_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(SupportFixture.FAQ_ID))
				.andExpect(jsonPath("$.data.question").value(SupportFixture.FAQ_QUESTION))
				.andExpect(jsonPath("$.data.answer").value(SupportFixture.FAQ_ANSWER))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@ParameterizedTest
	@ValueSource(strings = {"question", "answer"})
	@DisplayName("FAQ 질문이나 답변이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createFaqBlankText(String blankField) throws Exception {
		// given
		String question = blankField.equals("question") ? " " : SupportFixture.FAQ_QUESTION;
		String answer = blankField.equals("answer") ? " " : SupportFixture.FAQ_ANSWER;

		// when
		ResultActions result = postJson("/api/dev/faqs", SupportFixture.faqJson(question, answer));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(faqWriteService, never()).create(any());
	}

	@Test
	@DisplayName("FAQ를 단건 조회하면 200과 FAQ를 반환한다")
	void getFaq() throws Exception {
		// given
		given(faqReadService.getById(SupportFixture.FAQ_ID)).willReturn(SupportFixture.faqResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/faqs/{id}", SupportFixture.FAQ_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(SupportFixture.FAQ_ID))
				.andExpect(jsonPath("$.data.question").value(SupportFixture.FAQ_QUESTION));
	}

	@Test
	@DisplayName("없는 FAQ를 조회하면 404 NOT_FOUND를 반환한다")
	void getFaqNotFound() throws Exception {
		// given
		given(faqReadService.getById(SupportFixture.FAQ_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/faqs/{id}", SupportFixture.FAQ_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("문의를 생성하면 201과 Location, 생성된 문의를 반환한다")
	void createInquiry() throws Exception {
		// given
		given(inquiryWriteService.create(any(CreateInquiryRequest.class)))
				.willReturn(SupportFixture.inquiryResponse());

		// when
		ResultActions result = postJson("/api/dev/inquiries", SupportFixture.inquiryJson(
				SupportFixture.AUTHOR_ID, SupportFixture.INQUIRY_TITLE, SupportFixture.INQUIRY_CONTENT));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/dev/inquiries/" + SupportFixture.INQUIRY_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(SupportFixture.INQUIRY_ID))
				.andExpect(jsonPath("$.data.authorId").value(SupportFixture.AUTHOR_ID))
				.andExpect(jsonPath("$.data.title").value(SupportFixture.INQUIRY_TITLE))
				.andExpect(jsonPath("$.data.content").value(SupportFixture.INQUIRY_CONTENT))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@Test
	@DisplayName("문의 작성자 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createInquiryWithoutAuthorId() throws Exception {
		// given
		String body = SupportFixture.inquiryJson(null, SupportFixture.INQUIRY_TITLE, SupportFixture.INQUIRY_CONTENT);

		// when
		ResultActions result = postJson("/api/dev/inquiries", body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(inquiryWriteService, never()).create(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {"title", "content"})
	@DisplayName("문의 제목이나 내용이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createInquiryBlankText(String blankField) throws Exception {
		// given
		String title = blankField.equals("title") ? " " : SupportFixture.INQUIRY_TITLE;
		String content = blankField.equals("content") ? " " : SupportFixture.INQUIRY_CONTENT;

		// when
		ResultActions result = postJson("/api/dev/inquiries",
				SupportFixture.inquiryJson(SupportFixture.AUTHOR_ID, title, content));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(inquiryWriteService, never()).create(any());
	}

	@Test
	@DisplayName("없는 작성자로 문의를 생성하면 404 NOT_FOUND를 반환한다")
	void createInquiryWithMissingAuthor() throws Exception {
		// given
		given(inquiryWriteService.create(any(CreateInquiryRequest.class)))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = postJson("/api/dev/inquiries", SupportFixture.inquiryJson(
				SupportFixture.AUTHOR_ID, SupportFixture.INQUIRY_TITLE, SupportFixture.INQUIRY_CONTENT));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("문의를 단건 조회하면 200과 문의를 반환한다")
	void getInquiry() throws Exception {
		// given
		given(inquiryReadService.getById(SupportFixture.INQUIRY_ID)).willReturn(SupportFixture.inquiryResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/inquiries/{id}", SupportFixture.INQUIRY_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(SupportFixture.INQUIRY_ID))
				.andExpect(jsonPath("$.data.authorId").value(SupportFixture.AUTHOR_ID));
	}

	@Test
	@DisplayName("없는 문의를 조회하면 404 NOT_FOUND를 반환한다")
	void getInquiryNotFound() throws Exception {
		// given
		given(inquiryReadService.getById(SupportFixture.INQUIRY_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/inquiries/{id}", SupportFixture.INQUIRY_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@ParameterizedTest
	@ValueSource(strings = {"/api/dev/notices/abc", "/api/dev/faqs/abc", "/api/dev/inquiries/abc"})
	@DisplayName("support ID가 숫자가 아니면 400 INVALID_REQUEST를 반환한다")
	void getByInvalidIdType(String path) throws Exception {
		// given
		String target = path;

		// when
		ResultActions result = mockMvc.perform(get(target));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
	}

	private ResultActions postJson(String path, String body) throws Exception {
		return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
