package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 작성자는 토큰에서 정한다. analysisId가 있으면 AI로 만든 레시피다. */
public record RegisterRecipeRequest(@NotNull Long menuId, @NotBlank @Size(max = 200) String title,
		@NotEmpty List<@NotNull @Valid RecipeOptionRequest> options, String analysisId) {
}
