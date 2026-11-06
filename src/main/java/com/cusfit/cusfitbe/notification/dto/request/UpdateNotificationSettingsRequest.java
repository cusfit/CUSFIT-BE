package com.cusfit.cusfitbe.notification.dto.request;

import jakarta.validation.constraints.NotNull;

/** "전체" 스위치는 세 항목을 함께 바꾸는 화면 동작으로 보고 따로 받지 않는다. */
public record UpdateNotificationSettingsRequest(@NotNull Boolean activity, @NotNull Boolean news,
		@NotNull Boolean service) {
}
