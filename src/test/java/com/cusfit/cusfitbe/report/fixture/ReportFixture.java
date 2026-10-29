package com.cusfit.cusfitbe.report.fixture;

import com.cusfit.cusfitbe.recipe.fixture.RecipeFixture;
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.entity.Report;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class ReportFixture {

	public static final Long REPORT_ID = 200L;
	public static final Long REPORTER_ID = UserFixture.ID;
	public static final Long RECIPE_ID = RecipeFixture.RECIPE_ID;
	public static final String CONTENT = "레시피 내용이 실제 메뉴와 다릅니다";
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private ReportFixture() {
	}

	public static Report report() {
		return report(REPORTER_ID, RECIPE_ID);
	}

	public static Report report(Long reporterId, Long recipeId) {
		return Report.create(reporterId, recipeId, CONTENT);
	}

	/** 저장된 상태를 흉내 내기 위해 BaseCreatedEntity의 id/createdAt을 채운다. 서비스 단위 테스트 전용. */
	public static Report persisted(Report report, Long id) {
		ReflectionTestUtils.setField(report, "id", id);
		ReflectionTestUtils.setField(report, "createdAt", CREATED_AT);
		return report;
	}

	public static Report persistedReport() {
		return persisted(report(), REPORT_ID);
	}

	public static CreateReportRequest createRequest() {
		return new CreateReportRequest(REPORTER_ID, RECIPE_ID, CONTENT);
	}

	public static ReportResponse reportResponse() {
		return new ReportResponse(REPORT_ID, REPORTER_ID, RECIPE_ID, CONTENT, CREATED_AT);
	}

	public static String createRequestJson() {
		return createRequestJson(REPORTER_ID, RECIPE_ID, CONTENT);
	}

	public static String createRequestJson(Long reporterId, Long recipeId, String content) {
		return "{\"reporterId\": " + reporterId
				+ ", \"recipeId\": " + recipeId
				+ ", \"content\": " + (content == null ? "null" : "\"" + content + "\"") + "}";
	}
}
