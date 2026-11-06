package com.cusfit.cusfitbe.recipe.dto.response;

import java.util.List;

public record RecipeOptionGroupsResponse(List<RecipeOptionDetailResponse> top,
		List<RecipeOptionDetailResponse> middle, List<RecipeOptionDetailResponse> bottom) {
}
