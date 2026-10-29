package com.cusfit.cusfitbe.recipe.service;

import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;

public interface RecipeWriteService {

	RecipeResponse create(CreateRecipeRequest request);
}
