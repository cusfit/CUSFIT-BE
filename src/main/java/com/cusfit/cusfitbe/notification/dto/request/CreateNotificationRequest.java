package com.cusfit.cusfitbe.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(@NotNull Long recipientId, @NotBlank String title,
		@NotBlank String content) {
}
