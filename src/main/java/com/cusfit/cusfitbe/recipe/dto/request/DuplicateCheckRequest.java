package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DuplicateCheckRequest(@NotNull Long menuId,
		@NotNull List<@NotNull @Valid RecipeOptionRequest> options) {
}
