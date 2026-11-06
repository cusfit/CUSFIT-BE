package com.cusfit.cusfitbe.recipe.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Recipe")
public interface RecipeApiControllerDocs {

	@Operation(summary = "R1. AI 분석 (미구현)", description = "multipart/form-data. image 또는 text 중 하나는 필수.")
	ResponseEntity<ApiResponse<AiAnalysisResponse>> analyze(Jwt jwt, AiAnalysisRequest request);

	@Operation(summary = "R2. 중복 확인 (미구현)", description = "본인 레시피 중 기본 메뉴와 그룹별 옵션 집합이 같은 것을 찾는다.")
	ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkDuplicate(Jwt jwt, DuplicateCheckRequest request);

	@Operation(summary = "R3. 레시피 저장 (미구현)")
	ResponseEntity<ApiResponse<RecipeDetailResponse>> register(Jwt jwt, RegisterRecipeRequest request);

	@Operation(summary = "R4. AI 피드백 (미구현)")
	ResponseEntity<ApiResponse<Void>> submitAiFeedback(Jwt jwt, Long recipeId, AiFeedbackRequest request);

	@Operation(summary = "R5. 레시피 목록·검색 (미구현)", description = "로그인 선택. 인기순은 page, 최신순은 cursor.")
	ResponseEntity<ApiResponse<PageResponse<RecipeSummaryResponse>>> search(Jwt jwt, RecipeSearchRequest request);

	@Operation(summary = "R6. 레시피 상세 (미구현)", description = "로그인 선택.")
	ResponseEntity<ApiResponse<RecipeDetailResponse>> getDetail(Jwt jwt, Long recipeId);

	@Operation(summary = "R7. 레시피 삭제 (미구현)")
	ResponseEntity<Void> delete(Jwt jwt, Long recipeId);

	@Operation(summary = "R8. 좋아요 (미구현)", description = "이미 눌렀어도 성공하고 카운트는 그대로다.")
	ResponseEntity<ApiResponse<RecipeLikeResponse>> like(Jwt jwt, Long recipeId);

	@Operation(summary = "R8. 좋아요 취소 (미구현)")
	ResponseEntity<ApiResponse<RecipeLikeResponse>> unlike(Jwt jwt, Long recipeId);

	@Operation(summary = "R9. 저장 (미구현)", description = "이미 저장했어도 성공하고 카운트는 그대로다.")
	ResponseEntity<ApiResponse<RecipeSaveResponse>> save(Jwt jwt, Long recipeId);

	@Operation(summary = "R9. 저장 취소 (미구현)")
	ResponseEntity<ApiResponse<RecipeSaveResponse>> unsave(Jwt jwt, Long recipeId);
}
