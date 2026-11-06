package com.cusfit.cusfitbe.report.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.report.dto.request.ReportRecipeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Report")
public interface ReportApiControllerDocs {

	@Operation(summary = "P1. 레시피 신고 (미구현)")
	ResponseEntity<ApiResponse<Void>> reportRecipe(Jwt jwt, Long recipeId, ReportRecipeRequest request);
}
