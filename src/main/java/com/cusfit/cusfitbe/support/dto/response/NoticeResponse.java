package com.cusfit.cusfitbe.support.dto.response;

import com.cusfit.cusfitbe.support.entity.Notice;
import java.time.Instant;

public record NoticeResponse(Long id, String title, String content, Instant createdAt) {

	public static NoticeResponse from(Notice entity) {
		return new NoticeResponse(entity.getId(), entity.getTitle(), entity.getContent(), entity.getCreatedAt());
	}
}
