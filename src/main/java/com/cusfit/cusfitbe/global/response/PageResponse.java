package com.cusfit.cusfitbe.global.response;

import java.util.List;

/**
 * 목록 응답. 인기순은 {@code hasNext}만, 최신순은 {@code nextCursor}까지, 개수가 화면에 보이는 목록만 {@code totalCount}를
 * 채운다 (docs/API_DOCS.md 페이지네이션).
 */
public record PageResponse<T>(List<T> items, boolean hasNext, String nextCursor, Long totalCount) {
}
