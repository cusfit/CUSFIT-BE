package com.cusfit.cusfitbe.recipe.dto.response;

public record DuplicateCheckResponse(boolean duplicated, RecipeSummaryResponse existingRecipe) {
}
