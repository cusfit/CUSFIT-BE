package com.cusfit.cusfitbe.report.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.fixture.ReportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ReportTest {

	@Test
	@DisplayName("유효한 값으로 생성하면 신고자, 레시피, 내용을 가진 신고가 생성된다")
	void create() {
		// when
		Report report = Report.create(ReportFixture.REPORTER_ID, ReportFixture.RECIPE_ID, ReportFixture.CONTENT);

		// then
		assertThat(report).isInstanceOf(BaseCreatedEntity.class);
		assertThat(report.getId()).isNull();
		assertThat(report.getReporterId()).isEqualTo(ReportFixture.REPORTER_ID);
		assertThat(report.getRecipeId()).isEqualTo(ReportFixture.RECIPE_ID);
		assertThat(report.getContent()).isEqualTo(ReportFixture.CONTENT);
	}

	@Test
	@DisplayName("신고자 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullReporterId() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> ReportFixture.report(null, ReportFixture.RECIPE_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("레시피 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullRecipeId() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> ReportFixture.report(ReportFixture.REPORTER_ID, null));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("내용이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankContent(String content) {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Report.create(ReportFixture.REPORTER_ID, ReportFixture.RECIPE_ID, content));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("저장된 신고를 응답으로 변환하면 id, 신고자, 레시피, 내용, 생성 시각 순서의 값을 가진다")
	void toResponse() {
		// given
		Report report = ReportFixture.persistedReport();

		// when
		ReportResponse response = ReportResponse.from(report);

		// then
		assertThat(response).isEqualTo(ReportFixture.reportResponse());
	}
}
