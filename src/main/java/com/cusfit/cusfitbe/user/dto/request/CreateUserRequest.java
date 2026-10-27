package com.cusfit.cusfitbe.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String nickname) {
}
