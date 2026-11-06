package com.cusfit.cusfitbe.recipe.service;

import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.recipe.dto.request.DuplicateCheckRequest;
import com.cusfit.cusfitbe.recipe.dto.request.MyRecipePageRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeSearchRequest;
import com.cusfit.cusfitbe.recipe.dto.response.DuplicateCheckResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeDetailResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse;

public interface RecipeReadService {

	RecipeResponse getById(Long id);

	/** authSubject는 비로그인이면 null이다. */
	PageResponse<RecipeSummaryResponse> search(String authSubject, RecipeSearchRequest request);

	/** authSubject는 비로그인이면 null이다. */
	RecipeDetailResponse getDetail(String authSubject, Long recipeId);

	DuplicateCheckResponse checkDuplicate(String authSubject, DuplicateCheckRequest request);

	PageResponse<RecipeSummaryResponse> getMyRecipes(String authSubject, MyRecipePageRequest request);

	PageResponse<RecipeSummaryResponse> getSavedRecipes(String authSubject, MyRecipePageRequest request);
}
