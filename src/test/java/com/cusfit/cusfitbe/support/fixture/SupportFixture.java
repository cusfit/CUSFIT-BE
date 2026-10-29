package com.cusfit.cusfitbe.support.fixture;

import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.entity.Faq;
import com.cusfit.cusfitbe.support.entity.Inquiry;
import com.cusfit.cusfitbe.support.entity.Notice;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class SupportFixture {

	public static final Long NOTICE_ID = 40L;
	public static final Long FAQ_ID = 41L;
	public static final Long INQUIRY_ID = 42L;
	public static final Long AUTHOR_ID = 1L;
	public static final String NOTICE_TITLE = "서비스 점검 안내";
	public static final String NOTICE_CONTENT = "1월 1일 새벽에 점검이 진행됩니다.";
	public static final String FAQ_QUESTION = "레시피는 어떻게 등록하나요?";
	public static final String FAQ_ANSWER = "메뉴를 고르고 옵션을 추가하면 됩니다.";
	public static final String INQUIRY_TITLE = "레시피 등록 문의";
	public static final String INQUIRY_CONTENT = "레시피가 저장되지 않습니다.";
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private SupportFixture() {
	}

	public static Notice notice() {
		return Notice.create(NOTICE_TITLE, NOTICE_CONTENT);
	}

	public static Faq faq() {
		return Faq.create(FAQ_QUESTION, FAQ_ANSWER);
	}

	public static Inquiry inquiry() {
		return inquiry(AUTHOR_ID);
	}

	public static Inquiry inquiry(Long authorId) {
		return Inquiry.create(authorId, INQUIRY_TITLE, INQUIRY_CONTENT);
	}

	/** 저장된 상태를 흉내 내기 위해 BaseCreatedEntity의 id/createdAt을 채운다. 서비스 단위 테스트 전용. */
	public static <T> T persisted(T entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id);
		ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
		return entity;
	}

	public static CreateNoticeRequest createNoticeRequest() {
		return new CreateNoticeRequest(NOTICE_TITLE, NOTICE_CONTENT);
	}

	public static CreateFaqRequest createFaqRequest() {
		return new CreateFaqRequest(FAQ_QUESTION, FAQ_ANSWER);
	}

	public static CreateInquiryRequest createInquiryRequest() {
		return new CreateInquiryRequest(AUTHOR_ID, INQUIRY_TITLE, INQUIRY_CONTENT);
	}

	public static NoticeResponse noticeResponse() {
		return new NoticeResponse(NOTICE_ID, NOTICE_TITLE, NOTICE_CONTENT, CREATED_AT);
	}

	public static FaqResponse faqResponse() {
		return new FaqResponse(FAQ_ID, FAQ_QUESTION, FAQ_ANSWER, CREATED_AT);
	}

	public static InquiryResponse inquiryResponse() {
		return new InquiryResponse(INQUIRY_ID, AUTHOR_ID, INQUIRY_TITLE, INQUIRY_CONTENT, CREATED_AT);
	}

	public static UserResponse authorResponse() {
		return new UserResponse(AUTHOR_ID, "커스핏유저", CREATED_AT);
	}

	public static String noticeJson(String title, String content) {
		return "{\"title\":" + quote(title) + ",\"content\":" + quote(content) + "}";
	}

	public static String faqJson(String question, String answer) {
		return "{\"question\":" + quote(question) + ",\"answer\":" + quote(answer) + "}";
	}

	public static String inquiryJson(Long authorId, String title, String content) {
		return "{\"authorId\":" + authorId
				+ ",\"title\":" + quote(title)
				+ ",\"content\":" + quote(content) + "}";
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}
