package com.cusfit.cusfitbe.recipe.dto.response;

import com.cusfit.cusfitbe.recipe.entity.OptionGroup;

public record RecipeOptionDetailResponse(String name, OptionGroup group, int position, Long presetId,
		Integer quantity) {
}
