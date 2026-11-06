package com.cusfit.cusfitbe.support.dto.response;

import com.cusfit.cusfitbe.support.entity.Notice;
import java.time.Instant;

public record NoticeSummaryResponse(Long id, String title, Instant createdAt) {

	public static NoticeSummaryResponse from(Notice entity) {
		return new NoticeSummaryResponse(entity.getId(), entity.getTitle(), entity.getCreatedAt());
	}
}
