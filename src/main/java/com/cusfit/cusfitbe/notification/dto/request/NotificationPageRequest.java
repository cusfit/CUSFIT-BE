package com.cusfit.cusfitbe.notification.dto.request;

import com.cusfit.cusfitbe.notification.entity.NotificationCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** tab이 없으면 전체 알림이다. size 기본 20, 최대 50은 API_DOCS의 추정값이다. */
public record NotificationPageRequest(NotificationCategory tab, String cursor, @Min(1) @Max(50) Integer size) {

	public NotificationPageRequest {
		size = size == null ? 20 : size;
	}
}
