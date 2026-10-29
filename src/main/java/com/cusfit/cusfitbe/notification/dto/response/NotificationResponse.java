package com.cusfit.cusfitbe.notification.dto.response;

import com.cusfit.cusfitbe.notification.entity.Notification;
import java.time.Instant;

public record NotificationResponse(Long id, Long recipientId, String title, String content, Instant createdAt) {

	public static NotificationResponse from(Notification entity) {
		return new NotificationResponse(entity.getId(), entity.getRecipientId(), entity.getTitle(),
				entity.getContent(), entity.getCreatedAt());
	}
}
