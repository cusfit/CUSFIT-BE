package com.cusfit.cusfitbe.recipe.service;

import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;

public interface RecipeReadService {

	RecipeResponse getById(Long id);
}
