package com.cusfit.cusfitbe.notification.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.request.DeleteDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.RegisterDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.UpdateNotificationSettingsRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;
import com.cusfit.cusfitbe.notification.entity.Notification;
import com.cusfit.cusfitbe.notification.repository.NotificationRepository;
import com.cusfit.cusfitbe.notification.service.NotificationWriteService;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationWriteServiceImpl implements NotificationWriteService {

	private final NotificationRepository repository;
	private final UserReadService userReadService;

	public NotificationWriteServiceImpl(NotificationRepository repository, UserReadService userReadService) {
		this.repository = repository;
		this.userReadService = userReadService;
	}

	@Override
	public NotificationResponse create(CreateNotificationRequest request) {
		userReadService.getById(request.recipientId());
		Notification entity = Notification.create(request.recipientId(), request.title(), request.content());
		return NotificationResponse.from(repository.save(entity));
	}

	// 아래 세 기능은 토큰 subject와 회원 연결(D1), 알림 설정·기기 토큰 테이블이 생기면 구현한다.

	@Override
	public NotificationSettingsResponse updateMySettings(String authSubject,
			UpdateNotificationSettingsRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public void registerDeviceToken(String authSubject, RegisterDeviceTokenRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public void deleteDeviceToken(String authSubject, DeleteDeviceTokenRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
