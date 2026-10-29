package com.cusfit.cusfitbe.support.dto.response;

import com.cusfit.cusfitbe.support.entity.Faq;
import java.time.Instant;

public record FaqResponse(Long id, String question, String answer, Instant createdAt) {

	public static FaqResponse from(Faq entity) {
		return new FaqResponse(entity.getId(), entity.getQuestion(), entity.getAnswer(), entity.getCreatedAt());
	}
}
