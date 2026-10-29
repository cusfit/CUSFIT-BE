package com.cusfit.cusfitbe.report.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.fixture.ReportFixture;
import com.cusfit.cusfitbe.report.repository.ReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportReadServiceImplTest {

	@Mock
	private ReportRepository reportRepository;

	@InjectMocks
	private ReportReadServiceImpl reportReadService;

	@Test
	@DisplayName("존재하는 신고 ID로 조회하면 신고 응답을 반환한다")
	void getById() {
		// given
		given(reportRepository.findById(ReportFixture.REPORT_ID))
				.willReturn(Optional.of(ReportFixture.persistedReport()));

		// when
		ReportResponse response = reportReadService.getById(ReportFixture.REPORT_ID);

		// then
		assertThat(response).isEqualTo(ReportFixture.reportResponse());
	}

	@Test
	@DisplayName("없는 신고 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getByIdNotFound() {
		// given
		given(reportRepository.findById(ReportFixture.REPORT_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> reportReadService.getById(ReportFixture.REPORT_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
