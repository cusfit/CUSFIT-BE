package com.cusfit.cusfitbe.recipe.dto.response;

import com.cusfit.cusfitbe.recipe.entity.Recipe;
import java.time.Instant;
import java.util.List;

public record RecipeResponse(Long id, Long authorId, Long menuId, String title,
		List<RecipeOptionResponse> options, Instant createdAt) {

	public static RecipeResponse from(Recipe entity) {
		List<RecipeOptionResponse> options = entity.getOptions().stream().map(RecipeOptionResponse::from).toList();
		return new RecipeResponse(entity.getId(), entity.getAuthorId(), entity.getMenuId(), entity.getTitle(),
				options, entity.getCreatedAt());
	}
}
