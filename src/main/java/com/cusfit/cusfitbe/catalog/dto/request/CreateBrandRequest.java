package com.cusfit.cusfitbe.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandRequest(@NotBlank String name) {
}
