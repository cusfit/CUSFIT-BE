package com.cusfit.cusfitbe.support.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.entity.Faq;
import com.cusfit.cusfitbe.support.entity.Inquiry;
import com.cusfit.cusfitbe.support.entity.Notice;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.support.repository.FaqRepository;
import com.cusfit.cusfitbe.support.repository.InquiryRepository;
import com.cusfit.cusfitbe.support.repository.NoticeRepository;
import com.cusfit.cusfitbe.support.service.impl.FaqWriteServiceImpl;
import com.cusfit.cusfitbe.support.service.impl.InquiryWriteServiceImpl;
import com.cusfit.cusfitbe.support.service.impl.NoticeWriteServiceImpl;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportWriteServiceTest {

	@Mock
	private NoticeRepository noticeRepository;

	@Mock
	private FaqRepository faqRepository;

	@Mock
	private InquiryRepository inquiryRepository;

	@Mock
	private UserReadService userReadService;

	@InjectMocks
	private NoticeWriteServiceImpl noticeWriteService;

	@InjectMocks
	private FaqWriteServiceImpl faqWriteService;

	@InjectMocks
	private InquiryWriteServiceImpl inquiryWriteService;

	@Test
	@DisplayName("공지를 생성하면 저장된 공지 응답을 반환한다")
	void createNotice() {
		// given
		given(noticeRepository.save(any(Notice.class)))
				.willAnswer(invocation -> SupportFixture.persisted(invocation.getArgument(0), SupportFixture.NOTICE_ID));

		// when
		NoticeResponse response = noticeWriteService.create(SupportFixture.createNoticeRequest());

		// then
		assertThat(response).isEqualTo(SupportFixture.noticeResponse());
	}

	@Test
	@DisplayName("FAQ를 생성하면 저장된 FAQ 응답을 반환한다")
	void createFaq() {
		// given
		given(faqRepository.save(any(Faq.class)))
				.willAnswer(invocation -> SupportFixture.persisted(invocation.getArgument(0), SupportFixture.FAQ_ID));

		// when
		FaqResponse response = faqWriteService.create(SupportFixture.createFaqRequest());

		// then
		assertThat(response).isEqualTo(SupportFixture.faqResponse());
	}

	@Test
	@DisplayName("존재하는 작성자로 문의를 생성하면 작성자를 확인한 뒤 저장된 문의 응답을 반환한다")
	void createInquiry() {
		// given
		given(userReadService.getById(SupportFixture.AUTHOR_ID)).willReturn(SupportFixture.authorResponse());
		given(inquiryRepository.save(any(Inquiry.class)))
				.willAnswer(invocation -> SupportFixture.persisted(invocation.getArgument(0), SupportFixture.INQUIRY_ID));

		// when
		InquiryResponse response = inquiryWriteService.create(SupportFixture.createInquiryRequest());

		// then
		assertThat(response).isEqualTo(SupportFixture.inquiryResponse());
		InOrder order = inOrder(userReadService, inquiryRepository);
		order.verify(userReadService).getById(SupportFixture.AUTHOR_ID);
		order.verify(inquiryRepository).save(any(Inquiry.class));
	}

	@Test
	@DisplayName("없는 작성자로 문의를 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createInquiryWithMissingAuthor() {
		// given
		given(userReadService.getById(SupportFixture.AUTHOR_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> inquiryWriteService.create(SupportFixture.createInquiryRequest()));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		verify(inquiryRepository, never()).save(any());
	}
}
