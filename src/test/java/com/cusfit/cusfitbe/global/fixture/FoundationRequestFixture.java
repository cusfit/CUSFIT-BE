package com.cusfit.cusfitbe.global.fixture;

/** 공통 기반 테스트용 요청 본문. 테스트 내부 스텁 Controller의 요청(name, targetId, occurredAt) 형태를 따른다. */
public final class FoundationRequestFixture {

	public static final String VALID_NAME = "커스핏";
	public static final long VALID_TARGET_ID = 1L;
	public static final String VALID_OCCURRED_AT = "2026-01-01T00:00:00Z";

	private FoundationRequestFixture() {
	}

	public static String validJson() {
		return json("\"" + VALID_NAME + "\"", String.valueOf(VALID_TARGET_ID), "\"" + VALID_OCCURRED_AT + "\"");
	}

	public static String blankNameJson() {
		return json("\" \"", String.valueOf(VALID_TARGET_ID), "\"" + VALID_OCCURRED_AT + "\"");
	}

	public static String malformedJson() {
		return "{\"name\": \"" + VALID_NAME + "\", ";
	}

	public static String invalidInstantJson() {
		return json("\"" + VALID_NAME + "\"", String.valueOf(VALID_TARGET_ID), "\"not-a-date\"");
	}

	public static String invalidIdJson() {
		return json("\"" + VALID_NAME + "\"", "\"not-a-number\"", "\"" + VALID_OCCURRED_AT + "\"");
	}

	private static String json(String name, String targetId, String occurredAt) {
		return "{\"name\": " + name + ", \"targetId\": " + targetId + ", \"occurredAt\": " + occurredAt + "}";
	}
}
