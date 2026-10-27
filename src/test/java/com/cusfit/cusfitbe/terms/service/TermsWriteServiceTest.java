package com.cusfit.cusfitbe.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.entity.Terms;
import com.cusfit.cusfitbe.terms.fixture.TermsFixture;
import com.cusfit.cusfitbe.terms.repository.TermsRepository;
import com.cusfit.cusfitbe.terms.service.impl.TermsWriteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TermsWriteServiceTest {

	@Mock
	private TermsRepository termsRepository;

	@InjectMocks
	private TermsWriteServiceImpl termsWriteService;

	@Test
	@DisplayName("약관 생성 요청을 보내면 요청 값으로 약관을 저장하고 응답을 반환한다")
	void create() {
		// given
		given(termsRepository.save(any(Terms.class))).willAnswer(invocation -> {
			Terms terms = invocation.getArgument(0);
			ReflectionTestUtils.setField(terms, "id", 1L);
			ReflectionTestUtils.setField(terms, "createdAt", TermsFixture.CREATED_AT);
			return terms;
		});

		// when
		TermsResponse response = termsWriteService.create(TermsFixture.createRequest());

		// then
		ArgumentCaptor<Terms> captor = ArgumentCaptor.forClass(Terms.class);
		then(termsRepository).should().save(captor.capture());
		Terms saved = captor.getValue();
		assertThat(saved.getType()).isEqualTo(TermsFixture.TYPE);
		assertThat(saved.getVersion()).isEqualTo(TermsFixture.VERSION);
		assertThat(saved.getContentUrl()).isEqualTo(TermsFixture.CONTENT_URL);
		assertThat(saved.getEffectiveAt()).isEqualTo(TermsFixture.EFFECTIVE_AT);
		assertThat(response).isEqualTo(TermsFixture.response(1L));
	}

	@Test
	@DisplayName("빈 type으로 약관 생성을 요청하면 INVALID_REQUEST 예외가 발생하고 저장하지 않는다")
	void createWithBlankType() {
		// given
		var request = TermsFixture.createRequestWithType(" ");

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> termsWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		then(termsRepository).should(never()).save(any());
	}
}
