package com.cusfit.cusfitbe.notification.fixture;

import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.entity.Notification;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class NotificationFixture {

	public static final Long ID = 30L;
	public static final Long RECIPIENT_ID = 1L;
	public static final String TITLE = "레시피 알림";
	public static final String CONTENT = "새로운 레시피가 등록되었습니다.";
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private NotificationFixture() {
	}

	public static Notification notification() {
		return notification(RECIPIENT_ID);
	}

	public static Notification notification(Long recipientId) {
		return Notification.create(recipientId, TITLE, CONTENT);
	}

	public static Notification persistedNotification() {
		return persisted(notification());
	}

	/** 저장된 상태를 흉내 내기 위해 BaseCreatedEntity의 id/createdAt을 채운다. 서비스 단위 테스트 전용. */
	public static Notification persisted(Notification notification) {
		ReflectionTestUtils.setField(notification, "id", ID);
		ReflectionTestUtils.setField(notification, "createdAt", CREATED_AT);
		return notification;
	}

	public static CreateNotificationRequest createRequest() {
		return new CreateNotificationRequest(RECIPIENT_ID, TITLE, CONTENT);
	}

	public static NotificationResponse response() {
		return new NotificationResponse(ID, RECIPIENT_ID, TITLE, CONTENT, CREATED_AT);
	}

	public static UserResponse recipientResponse() {
		return new UserResponse(RECIPIENT_ID, "커스핏유저", CREATED_AT);
	}

	public static String createRequestJson(Long recipientId, String title, String content) {
		return "{\"recipientId\":" + recipientId
				+ ",\"title\":" + quote(title)
				+ ",\"content\":" + quote(content) + "}";
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}
