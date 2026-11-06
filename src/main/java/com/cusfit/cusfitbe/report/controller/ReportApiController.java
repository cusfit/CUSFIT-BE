package com.cusfit.cusfitbe.report.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.report.controller.docs.ReportApiControllerDocs;
import com.cusfit.cusfitbe.report.dto.request.ReportRecipeRequest;
import com.cusfit.cusfitbe.report.service.ReportWriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/reports")
public class ReportApiController implements ReportApiControllerDocs {

	private final ReportWriteService writeService;

	public ReportApiController(ReportWriteService writeService) {
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> reportRecipe(@AuthenticationPrincipal Jwt jwt, @PathVariable Long recipeId,
			@Valid @RequestBody ReportRecipeRequest request) {
		writeService.reportRecipe(jwt.getSubject(), recipeId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
	}
}
