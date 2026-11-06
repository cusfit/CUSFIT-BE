package com.cusfit.cusfitbe.notification.dto.response;

import com.cusfit.cusfitbe.notification.entity.NotificationCategory;
import java.time.Instant;

public record NotificationItemResponse(Long id, NotificationCategory category, String title, String content,
		Long targetRecipeId, Instant createdAt) {
}
