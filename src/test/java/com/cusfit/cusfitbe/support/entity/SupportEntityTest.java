package com.cusfit.cusfitbe.support.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SupportEntityTest {

	@Test
	@DisplayName("제목과 내용으로 공지를 생성하면 값을 가진 공지가 만들어진다")
	void createNotice() {
		// given
		String title = SupportFixture.NOTICE_TITLE;

		// when
		Notice notice = Notice.create(title, SupportFixture.NOTICE_CONTENT);

		// then
		assertThat(notice.getId()).isNull();
		assertThat(notice.getTitle()).isEqualTo(title);
		assertThat(notice.getContent()).isEqualTo(SupportFixture.NOTICE_CONTENT);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("공지 제목이나 내용이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankNoticeText(String blank) {
		// given
		Executable blankTitle = () -> Notice.create(blank, SupportFixture.NOTICE_CONTENT);
		Executable blankContent = () -> Notice.create(SupportFixture.NOTICE_TITLE, blank);

		// when & then
		assertInvalidRequest(blankTitle);
		assertInvalidRequest(blankContent);
	}

	@Test
	@DisplayName("질문과 답변으로 FAQ를 생성하면 값을 가진 FAQ가 만들어진다")
	void createFaq() {
		// given
		String question = SupportFixture.FAQ_QUESTION;

		// when
		Faq faq = Faq.create(question, SupportFixture.FAQ_ANSWER);

		// then
		assertThat(faq.getId()).isNull();
		assertThat(faq.getQuestion()).isEqualTo(question);
		assertThat(faq.getAnswer()).isEqualTo(SupportFixture.FAQ_ANSWER);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("FAQ 질문이나 답변이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankFaqText(String blank) {
		// given
		Executable blankQuestion = () -> Faq.create(blank, SupportFixture.FAQ_ANSWER);
		Executable blankAnswer = () -> Faq.create(SupportFixture.FAQ_QUESTION, blank);

		// when & then
		assertInvalidRequest(blankQuestion);
		assertInvalidRequest(blankAnswer);
	}

	@Test
	@DisplayName("작성자 ID, 제목, 내용으로 문의를 생성하면 값을 가진 문의가 만들어진다")
	void createInquiry() {
		// given
		Long authorId = SupportFixture.AUTHOR_ID;

		// when
		Inquiry inquiry = Inquiry.create(authorId, SupportFixture.INQUIRY_TITLE, SupportFixture.INQUIRY_CONTENT);

		// then
		assertThat(inquiry.getId()).isNull();
		assertThat(inquiry.getAuthorId()).isEqualTo(authorId);
		assertThat(inquiry.getTitle()).isEqualTo(SupportFixture.INQUIRY_TITLE);
		assertThat(inquiry.getContent()).isEqualTo(SupportFixture.INQUIRY_CONTENT);
	}

	@Test
	@DisplayName("문의 작성자 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullInquiryAuthorId() {
		// given
		Long authorId = null;

		// when & then
		assertInvalidRequest(
				() -> Inquiry.create(authorId, SupportFixture.INQUIRY_TITLE, SupportFixture.INQUIRY_CONTENT));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("문의 제목이나 내용이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankInquiryText(String blank) {
		// given
		Executable blankTitle = () -> Inquiry.create(SupportFixture.AUTHOR_ID, blank, SupportFixture.INQUIRY_CONTENT);
		Executable blankContent = () -> Inquiry.create(SupportFixture.AUTHOR_ID, SupportFixture.INQUIRY_TITLE, blank);

		// when & then
		assertInvalidRequest(blankTitle);
		assertInvalidRequest(blankContent);
	}

	private static void assertInvalidRequest(Executable executable) {
		BusinessException exception = assertThrows(BusinessException.class, executable);
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
