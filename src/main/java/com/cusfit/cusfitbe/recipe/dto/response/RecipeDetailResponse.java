package com.cusfit.cusfitbe.recipe.dto.response;

import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse.AuthorRef;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse.BrandRef;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse.MenuRef;
import java.time.Instant;
import java.util.List;

/** liked·saved는 비로그인이면 false다. */
public record RecipeDetailResponse(Long id, String title, MenuRef menu, BrandRef brand, AuthorRef author,
		List<String> optionPreview, RecipeOptionGroupsResponse options, boolean isMine, long likeCount,
		long saveCount, boolean liked, boolean saved, Instant createdAt) {
}
