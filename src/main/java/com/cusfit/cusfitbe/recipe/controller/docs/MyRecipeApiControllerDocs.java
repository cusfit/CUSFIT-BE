package com.cusfit.cusfitbe.recipe.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.recipe.dto.request.MyRecipePageRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Recipe")
public interface MyRecipeApiControllerDocs {

	@Operation(summary = "R10. 내 레시피 (미구현)", description = "최신순, 커서 방식. totalCount 포함.")
	ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> getMyRecipes(Jwt jwt,
			MyRecipePageRequest request);

	@Operation(summary = "R11. 저장한 레시피 (미구현)", description = "최신순, 커서 방식. totalCount 포함.")
	ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> getSavedRecipes(Jwt jwt,
			MyRecipePageRequest request);
}
