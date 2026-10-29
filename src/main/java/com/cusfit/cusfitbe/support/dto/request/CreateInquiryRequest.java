package com.cusfit.cusfitbe.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInquiryRequest(@NotNull Long authorId, @NotBlank String title, @NotBlank String content) {
}
