package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRecipeOptionRequest(@NotBlank String name) {
}
