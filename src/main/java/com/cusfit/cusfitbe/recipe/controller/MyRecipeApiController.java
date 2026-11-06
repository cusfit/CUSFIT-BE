package com.cusfit.cusfitbe.recipe.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.recipe.controller.docs.MyRecipeApiControllerDocs;
import com.cusfit.cusfitbe.recipe.dto.request.MyRecipePageRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class MyRecipeApiController implements MyRecipeApiControllerDocs {

	private final RecipeReadService readService;

	public MyRecipeApiController(RecipeReadService readService) {
		this.readService = readService;
	}

	@Override
	@GetMapping("/recipes")
	public ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> getMyRecipes(
			@AuthenticationPrincipal Jwt jwt, @Valid @ModelAttribute MyRecipePageRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getMyRecipes(jwt.getSubject(), request)));
	}

	@Override
	@GetMapping("/saved-recipes")
	public ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> getSavedRecipes(
			@AuthenticationPrincipal Jwt jwt, @Valid @ModelAttribute MyRecipePageRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getSavedRecipes(jwt.getSubject(), request)));
	}
}
