package com.cusfit.cusfitbe.recipe.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.recipe.controller.docs.RecipeApiControllerDocs;
import com.cusfit.cusfitbe.recipe.dto.request.AiAnalysisRequest;
import com.cusfit.cusfitbe.recipe.dto.request.AiFeedbackRequest;
import com.cusfit.cusfitbe.recipe.dto.request.DuplicateCheckRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeSearchRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RegisterRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.AiAnalysisResponse;
import com.cusfit.cusfitbe.recipe.dto.response.DuplicateCheckResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeDetailResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeLikeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSaveResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse;
import com.cusfit.cusfitbe.recipe.service.RecipeAnalysisService;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import com.cusfit.cusfitbe.recipe.service.RecipeWriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeApiController implements RecipeApiControllerDocs {

	private final RecipeReadService readService;
	private final RecipeWriteService writeService;
	private final RecipeAnalysisService analysisService;

	public RecipeApiController(RecipeReadService readService, RecipeWriteService writeService,
			RecipeAnalysisService analysisService) {
		this.readService = readService;
		this.writeService = writeService;
		this.analysisService = analysisService;
	}

	@Override
	@PostMapping(value = "/ai-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<AiAnalysisResponse>> analyze(@AuthenticationPrincipal Jwt jwt,
			@Valid @ModelAttribute AiAnalysisRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(analysisService.analyze(jwt.getSubject(), request)));
	}

	@Override
	@PostMapping("/duplicate-check")
	public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkDuplicate(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody DuplicateCheckRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.checkDuplicate(jwt.getSubject(), request)));
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<RecipeDetailResponse>> register(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody RegisterRecipeRequest request) {
		RecipeDetailResponse response = writeService.register(jwt.getSubject(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@Override
	@PostMapping("/{recipeId}/ai-feedback")
	public ResponseEntity<ApiResponse<Void>> submitAiFeedback(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId, @Valid @RequestBody AiFeedbackRequest request) {
		writeService.submitAiFeedback(jwt.getSubject(), recipeId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
	}

	@Override
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> search(@AuthenticationPrincipal Jwt jwt,
			@Valid @ModelAttribute RecipeSearchRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.search(subjectOrNull(jwt), request)));
	}

	@Override
	@GetMapping("/{recipeId}")
	public ResponseEntity<ApiResponse<RecipeDetailResponse>> getDetail(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getDetail(subjectOrNull(jwt), recipeId)));
	}

	@Override
	@DeleteMapping("/{recipeId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long recipeId) {
		writeService.delete(jwt.getSubject(), recipeId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PutMapping("/{recipeId}/like")
	public ResponseEntity<ApiResponse<RecipeLikeResponse>> like(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.like(jwt.getSubject(), recipeId)));
	}

	@Override
	@DeleteMapping("/{recipeId}/like")
	public ResponseEntity<ApiResponse<RecipeLikeResponse>> unlike(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.unlike(jwt.getSubject(), recipeId)));
	}

	@Override
	@PutMapping("/{recipeId}/save")
	public ResponseEntity<ApiResponse<RecipeSaveResponse>> save(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.save(jwt.getSubject(), recipeId)));
	}

	@Override
	@DeleteMapping("/{recipeId}/save")
	public ResponseEntity<ApiResponse<RecipeSaveResponse>> unsave(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long recipeId) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.unsave(jwt.getSubject(), recipeId)));
	}

	private static String subjectOrNull(Jwt jwt) {
		return jwt == null ? null : jwt.getSubject();
	}
}
