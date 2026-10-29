package com.cusfit.cusfitbe.report.dto.response;

import com.cusfit.cusfitbe.report.entity.Report;
import java.time.Instant;

public record ReportResponse(Long id, Long reporterId, Long recipeId, String content, Instant createdAt) {

	public static ReportResponse from(Report report) {
		return new ReportResponse(report.getId(), report.getReporterId(), report.getRecipeId(), report.getContent(),
				report.getCreatedAt());
	}
}
