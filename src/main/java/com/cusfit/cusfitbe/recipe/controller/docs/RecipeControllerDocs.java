package com.cusfit.cusfitbe.recipe.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface RecipeControllerDocs {

	@Operation(summary = "Create Recipe")
	ResponseEntity<ApiResponse<RecipeResponse>> create(CreateRecipeRequest request);

	@Operation(summary = "Get Recipe")
	ResponseEntity<ApiResponse<RecipeResponse>> getById(Long id);
}
