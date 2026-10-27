package com.cusfit.cusfitbe.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOptionPresetRequest(@NotNull Long brandId, @NotBlank String name) {
}
