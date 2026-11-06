package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 인기순은 page, 최신순은 cursor로 넘긴다 (API-01). size 기본 20, 최대 50은 API_DOCS의 추정값이다. */
public record RecipeSearchRequest(String keyword, Long brandId, RecipeSort sort, @Min(0) Integer page, String cursor,
		@Min(1) @Max(50) Integer size) {

	public RecipeSearchRequest {
		sort = sort == null ? RecipeSort.POPULAR : sort;
		page = page == null ? 0 : page;
		size = size == null ? 20 : size;
	}
}
