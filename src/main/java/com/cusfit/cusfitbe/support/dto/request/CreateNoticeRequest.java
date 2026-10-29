package com.cusfit.cusfitbe.support.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateNoticeRequest(@NotBlank String title, @NotBlank String content) {
}
