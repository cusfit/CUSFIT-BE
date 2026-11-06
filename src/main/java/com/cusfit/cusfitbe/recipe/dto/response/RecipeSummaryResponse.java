package com.cusfit.cusfitbe.recipe.dto.response;

import java.time.Instant;
import java.util.List;

/** 목록 카드. 메뉴·브랜드·작성자 이름은 다른 도메인에서 ID로 모아 조회해 합친다 (API-04). */
public record RecipeSummaryResponse(Long id, String title, MenuRef menu, BrandRef brand, AuthorRef author,
		List<String> optionPreview, long saveCount, Instant createdAt) {

	public record MenuRef(Long id, String name) {
	}

	public record BrandRef(Long id, String name) {
	}

	public record AuthorRef(Long id, String nickname) {
	}
}
