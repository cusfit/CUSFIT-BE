package com.cusfit.cusfitbe.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.fixture.NotificationFixture;
import com.cusfit.cusfitbe.notification.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationReadServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationReadServiceImpl notificationReadService;

	@Test
	@DisplayName("존재하는 알림 ID로 조회하면 알림 응답을 반환한다")
	void getById() {
		// given
		given(notificationRepository.findById(NotificationFixture.ID))
				.willReturn(Optional.of(NotificationFixture.persistedNotification()));

		// when
		NotificationResponse response = notificationReadService.getById(NotificationFixture.ID);

		// then
		assertThat(response).isEqualTo(NotificationFixture.response());
	}

	@Test
	@DisplayName("없는 알림 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getByIdNotFound() {
		// given
		given(notificationRepository.findById(NotificationFixture.ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> notificationReadService.getById(NotificationFixture.ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
