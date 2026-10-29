package com.cusfit.cusfitbe.notification.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.repository.NotificationRepository;
import com.cusfit.cusfitbe.notification.service.NotificationReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationReadServiceImpl implements NotificationReadService {

	private final NotificationRepository repository;

	public NotificationReadServiceImpl(NotificationRepository repository) {
		this.repository = repository;
	}

	@Override
	public NotificationResponse getById(Long id) {
		return repository.findById(id)
				.map(NotificationResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
