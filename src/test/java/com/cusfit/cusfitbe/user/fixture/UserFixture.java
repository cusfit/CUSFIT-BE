package com.cusfit.cusfitbe.user.fixture;

import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.entity.User;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class UserFixture {

	public static final String NICKNAME = "커스핏유저";
	public static final Long ID = 1L;
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private UserFixture() {
	}

	public static User user() {
		return user(NICKNAME);
	}

	public static User user(String nickname) {
		return User.create(nickname);
	}

	/** 저장된 상태를 흉내 내기 위해 BaseCreatedEntity의 id/createdAt을 채운다. 서비스 단위 테스트 전용. */
	public static User persisted(User user, Long id) {
		ReflectionTestUtils.setField(user, "id", id);
		ReflectionTestUtils.setField(user, "createdAt", CREATED_AT);
		return user;
	}

	public static User persistedUser() {
		return persisted(user(), ID);
	}

	public static CreateUserRequest createRequest() {
		return createRequest(NICKNAME);
	}

	public static CreateUserRequest createRequest(String nickname) {
		return new CreateUserRequest(nickname);
	}

	public static String createRequestJson(String nickname) {
		return "{\"nickname\":" + (nickname == null ? "null" : "\"" + nickname + "\"") + "}";
	}
}
