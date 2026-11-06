package com.cusfit.cusfitbe.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteDeviceTokenRequest(@NotBlank String token) {
}
