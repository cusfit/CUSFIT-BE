package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 내 레시피·저장한 레시피 목록. 최신순 고정, 커서 방식이다. */
public record MyRecipePageRequest(Long brandId, String cursor, @Min(1) @Max(50) Integer size) {

	public MyRecipePageRequest {
		size = size == null ? 20 : size;
	}
}
