package com.cusfit.cusfitbe.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReportRequest(@NotNull Long reporterId, @NotNull Long recipeId, @NotBlank String content) {
}
