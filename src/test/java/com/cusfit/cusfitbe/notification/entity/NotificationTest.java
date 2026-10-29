package com.cusfit.cusfitbe.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.fixture.NotificationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationTest {

	@Test
	@DisplayName("수신자 ID, 제목, 내용으로 생성하면 값을 가진 알림이 만들어진다")
	void create() {
		// given
		Long recipientId = NotificationFixture.RECIPIENT_ID;

		// when
		Notification notification = Notification.create(recipientId, NotificationFixture.TITLE,
				NotificationFixture.CONTENT);

		// then
		assertThat(notification.getId()).isNull();
		assertThat(notification.getRecipientId()).isEqualTo(recipientId);
		assertThat(notification.getTitle()).isEqualTo(NotificationFixture.TITLE);
		assertThat(notification.getContent()).isEqualTo(NotificationFixture.CONTENT);
	}

	@Test
	@DisplayName("수신자 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullRecipientId() {
		// given
		Long recipientId = null;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Notification.create(recipientId, NotificationFixture.TITLE, NotificationFixture.CONTENT));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("제목이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankTitle(String title) {
		// given
		Long recipientId = NotificationFixture.RECIPIENT_ID;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Notification.create(recipientId, title, NotificationFixture.CONTENT));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("내용이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankContent(String content) {
		// given
		Long recipientId = NotificationFixture.RECIPIENT_ID;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Notification.create(recipientId, NotificationFixture.TITLE, content));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
