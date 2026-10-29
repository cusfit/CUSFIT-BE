package com.cusfit.cusfitbe.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.entity.Notification;
import com.cusfit.cusfitbe.notification.fixture.NotificationFixture;
import com.cusfit.cusfitbe.notification.repository.NotificationRepository;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationWriteServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UserReadService userReadService;

	@InjectMocks
	private NotificationWriteServiceImpl notificationWriteService;

	@Test
	@DisplayName("존재하는 수신자로 알림을 생성하면 저장된 알림 응답을 반환한다")
	void create() {
		// given
		CreateNotificationRequest request = NotificationFixture.createRequest();
		given(userReadService.getById(NotificationFixture.RECIPIENT_ID))
				.willReturn(NotificationFixture.recipientResponse());
		given(notificationRepository.save(any(Notification.class)))
				.willAnswer(invocation -> NotificationFixture.persisted(invocation.getArgument(0)));

		// when
		NotificationResponse response = notificationWriteService.create(request);

		// then
		assertThat(response).isEqualTo(NotificationFixture.response());
		verify(userReadService).getById(NotificationFixture.RECIPIENT_ID);
	}

	@Test
	@DisplayName("없는 수신자로 알림을 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createWithMissingRecipient() {
		// given
		CreateNotificationRequest request = NotificationFixture.createRequest();
		given(userReadService.getById(NotificationFixture.RECIPIENT_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> notificationWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		verify(notificationRepository, never()).save(any());
	}
}
