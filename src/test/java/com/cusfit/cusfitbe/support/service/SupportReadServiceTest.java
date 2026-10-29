package com.cusfit.cusfitbe.support.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.support.repository.FaqRepository;
import com.cusfit.cusfitbe.support.repository.InquiryRepository;
import com.cusfit.cusfitbe.support.repository.NoticeRepository;
import com.cusfit.cusfitbe.support.service.impl.FaqReadServiceImpl;
import com.cusfit.cusfitbe.support.service.impl.InquiryReadServiceImpl;
import com.cusfit.cusfitbe.support.service.impl.NoticeReadServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportReadServiceTest {

	@Mock
	private NoticeRepository noticeRepository;

	@Mock
	private FaqRepository faqRepository;

	@Mock
	private InquiryRepository inquiryRepository;

	@InjectMocks
	private NoticeReadServiceImpl noticeReadService;

	@InjectMocks
	private FaqReadServiceImpl faqReadService;

	@InjectMocks
	private InquiryReadServiceImpl inquiryReadService;

	@Test
	@DisplayName("존재하는 공지 ID로 조회하면 공지 응답을 반환한다")
	void getNotice() {
		// given
		given(noticeRepository.findById(SupportFixture.NOTICE_ID))
				.willReturn(Optional.of(SupportFixture.persisted(SupportFixture.notice(), SupportFixture.NOTICE_ID)));

		// when
		NoticeResponse response = noticeReadService.getById(SupportFixture.NOTICE_ID);

		// then
		assertThat(response).isEqualTo(SupportFixture.noticeResponse());
	}

	@Test
	@DisplayName("없는 공지 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getNoticeNotFound() {
		// given
		given(noticeRepository.findById(SupportFixture.NOTICE_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> noticeReadService.getById(SupportFixture.NOTICE_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("존재하는 FAQ ID로 조회하면 FAQ 응답을 반환한다")
	void getFaq() {
		// given
		given(faqRepository.findById(SupportFixture.FAQ_ID))
				.willReturn(Optional.of(SupportFixture.persisted(SupportFixture.faq(), SupportFixture.FAQ_ID)));

		// when
		FaqResponse response = faqReadService.getById(SupportFixture.FAQ_ID);

		// then
		assertThat(response).isEqualTo(SupportFixture.faqResponse());
	}

	@Test
	@DisplayName("없는 FAQ ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getFaqNotFound() {
		// given
		given(faqRepository.findById(SupportFixture.FAQ_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> faqReadService.getById(SupportFixture.FAQ_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("존재하는 문의 ID로 조회하면 문의 응답을 반환한다")
	void getInquiry() {
		// given
		given(inquiryRepository.findById(SupportFixture.INQUIRY_ID))
				.willReturn(Optional.of(SupportFixture.persisted(SupportFixture.inquiry(), SupportFixture.INQUIRY_ID)));

		// when
		InquiryResponse response = inquiryReadService.getById(SupportFixture.INQUIRY_ID);

		// then
		assertThat(response).isEqualTo(SupportFixture.inquiryResponse());
	}

	@Test
	@DisplayName("없는 문의 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getInquiryNotFound() {
		// given
		given(inquiryRepository.findById(SupportFixture.INQUIRY_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> inquiryReadService.getById(SupportFixture.INQUIRY_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
