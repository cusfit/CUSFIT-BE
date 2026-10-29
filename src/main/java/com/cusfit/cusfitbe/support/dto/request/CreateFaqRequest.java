package com.cusfit.cusfitbe.support.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFaqRequest(@NotBlank String question, @NotBlank String answer) {
}
