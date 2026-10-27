package com.cusfit.cusfitbe.user.dto.response;

import com.cusfit.cusfitbe.user.entity.User;
import java.time.Instant;

public record UserResponse(Long id, String nickname, Instant createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getNickname(), user.getCreatedAt());
	}
}
