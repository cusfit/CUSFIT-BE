package com.cusfit.cusfitbe.report.service;

import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.request.ReportRecipeRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;

public interface ReportWriteService {

	ReportResponse create(CreateReportRequest request);

	void reportRecipe(String authSubject, Long recipeId, ReportRecipeRequest request);
}
