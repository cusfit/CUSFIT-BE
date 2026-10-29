package com.cusfit.cusfitbe.notification.service.impl;

import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
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
}
