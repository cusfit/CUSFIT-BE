package com.cusfit.cusfitbe.user.dto.response;

import java.time.Instant;

public record MyProfileResponse(Long id, String nickname, String email, long myRecipeCount, Instant createdAt) {
}
