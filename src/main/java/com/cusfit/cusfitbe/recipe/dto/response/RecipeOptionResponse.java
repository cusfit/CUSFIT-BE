package com.cusfit.cusfitbe.recipe.dto.response;

import com.cusfit.cusfitbe.recipe.entity.RecipeOption;
import java.time.Instant;

public record RecipeOptionResponse(Long id, String name, int position, Instant createdAt) {

	public static RecipeOptionResponse from(RecipeOption entity) {
		return new RecipeOptionResponse(entity.getId(), entity.getName(), entity.getPosition(), entity.getCreatedAt());
	}
}
