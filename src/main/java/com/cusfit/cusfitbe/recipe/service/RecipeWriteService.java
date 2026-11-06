package com.cusfit.cusfitbe.recipe.service;

import com.cusfit.cusfitbe.recipe.dto.request.AiFeedbackRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RegisterRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeDetailResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeLikeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSaveResponse;

public interface RecipeWriteService {

	RecipeResponse create(CreateRecipeRequest request);

	RecipeDetailResponse register(String authSubject, RegisterRecipeRequest request);

	void delete(String authSubject, Long recipeId);

	void submitAiFeedback(String authSubject, Long recipeId, AiFeedbackRequest request);

	RecipeLikeResponse like(String authSubject, Long recipeId);

	RecipeLikeResponse unlike(String authSubject, Long recipeId);

	RecipeSaveResponse save(String authSubject, Long recipeId);

	RecipeSaveResponse unsave(String authSubject, Long recipeId);
}
