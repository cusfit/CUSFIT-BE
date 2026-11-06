package com.cusfit.cusfitbe.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SignUpRequest(@Nickname String nickname, @NotEmpty List<@NotNull Long> agreedTermsIds) {
}
