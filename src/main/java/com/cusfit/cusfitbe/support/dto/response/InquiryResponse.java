package com.cusfit.cusfitbe.support.dto.response;

import com.cusfit.cusfitbe.support.entity.Inquiry;
import java.time.Instant;

public record InquiryResponse(Long id, Long authorId, String title, String content, Instant createdAt) {

	public static InquiryResponse from(Inquiry entity) {
		return new InquiryResponse(entity.getId(), entity.getAuthorId(), entity.getTitle(), entity.getContent(), entity.getCreatedAt());
	}
}
