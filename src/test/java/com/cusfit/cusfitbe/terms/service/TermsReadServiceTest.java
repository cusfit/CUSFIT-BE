package com.cusfit.cusfitbe.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.fixture.TermsFixture;
import com.cusfit.cusfitbe.terms.repository.TermsRepository;
import com.cusfit.cusfitbe.terms.service.impl.TermsReadServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TermsReadServiceTest {

	@Mock
	private TermsRepository termsRepository;

	@InjectMocks
	private TermsReadServiceImpl termsReadService;

	@Test
	@DisplayName("존재하는 약관 ID로 조회하면 약관 응답을 반환한다")
	void getById() {
		// given
		given(termsRepository.findById(1L)).willReturn(Optional.of(TermsFixture.persistedTerms(1L)));

		// when
		TermsResponse response = termsReadService.getById(1L);

		// then
		assertThat(response).isEqualTo(TermsFixture.response(1L));
	}

	@Test
	@DisplayName("존재하지 않는 약관 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getByIdNotFound() {
		// given
		given(termsRepository.findById(99L)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> termsReadService.getById(99L));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
