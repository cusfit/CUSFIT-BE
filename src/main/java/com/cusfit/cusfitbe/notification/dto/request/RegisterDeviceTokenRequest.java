package com.cusfit.cusfitbe.notification.dto.request;

import com.cusfit.cusfitbe.notification.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceTokenRequest(@NotBlank String token, @NotNull DevicePlatform platform) {
}
