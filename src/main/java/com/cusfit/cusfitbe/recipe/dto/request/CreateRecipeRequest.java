package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateRecipeRequest(@NotNull Long authorId, @NotNull Long menuId, @NotBlank String title,
		@NotNull List<@NotNull @Valid CreateRecipeOptionRequest> options) {
}
