package com.cusfit.cusfitbe.report.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.entity.Report;
import com.cusfit.cusfitbe.report.fixture.ReportFixture;
import com.cusfit.cusfitbe.report.repository.ReportRepository;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportWriteServiceImplTest {

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private UserReadService userReadService;

	@Mock
	private RecipeReadService recipeReadService;

	@InjectMocks
	private ReportWriteServiceImpl reportWriteService;

	@Test
	@DisplayName("존재하는 신고자와 레시피로 생성하면 신고를 저장하고 신고 응답을 반환한다")
	void create() {
		// given
		CreateReportRequest request = ReportFixture.createRequest();
		given(userReadService.getById(ReportFixture.REPORTER_ID)).willReturn(reporterResponse());
		given(recipeReadService.getById(ReportFixture.RECIPE_ID)).willReturn(RecipeFixture.recipeResponse());
		given(reportRepository.save(any(Report.class)))
				.willAnswer(invocation -> ReportFixture.persisted(invocation.getArgument(0), ReportFixture.REPORT_ID));

		// when
		ReportResponse response = reportWriteService.create(request);

		// then
		ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
		then(reportRepository).should().save(captor.capture());
		Report saved = captor.getValue();
		assertThat(saved.getReporterId()).isEqualTo(ReportFixture.REPORTER_ID);
		assertThat(saved.getRecipeId()).isEqualTo(ReportFixture.RECIPE_ID);
		assertThat(saved.getContent()).isEqualTo(ReportFixture.CONTENT);
		assertThat(response).isEqualTo(ReportFixture.reportResponse());
	}

	@Test
	@DisplayName("없는 신고자로 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createWithMissingReporter() {
		// given
		CreateReportRequest request = ReportFixture.createRequest();
		given(userReadService.getById(ReportFixture.REPORTER_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));
		lenient().when(recipeReadService.getById(ReportFixture.RECIPE_ID)).thenReturn(RecipeFixture.recipeResponse());

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> reportWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		then(reportRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("없는 레시피로 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createWithMissingRecipe() {
		// given
		CreateReportRequest request = ReportFixture.createRequest();
		lenient().when(userReadService.getById(ReportFixture.REPORTER_ID)).thenReturn(reporterResponse());
		given(recipeReadService.getById(ReportFixture.RECIPE_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> reportWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		then(reportRepository).should(never()).save(any());
	}

	private static UserResponse reporterResponse() {
		return UserResponse.from(UserFixture.persistedUser());
	}
}
