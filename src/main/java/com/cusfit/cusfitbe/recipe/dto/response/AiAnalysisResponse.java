package com.cusfit.cusfitbe.recipe.dto.response;

import java.util.List;

/** 인식하지 못한 항목은 null로 두고 사용자가 직접 채운다. */
public record AiAnalysisResponse(String analysisId, Long brandId, Long menuId, String title,
		List<RecipeOptionDetailResponse> options) {
}
