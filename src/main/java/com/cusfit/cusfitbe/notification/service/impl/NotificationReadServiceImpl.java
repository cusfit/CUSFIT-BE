package com.cusfit.cusfitbe.notification.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.notification.dto.request.NotificationPageRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationItemResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;
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

	// 아래 두 기능은 토큰 subject와 회원 연결(D1), 알림 분류·대상 레시피 컬럼과 알림 설정 테이블이 생기면 구현한다.

	@Override
	public PageResponse<NotificationItemResponse> getMyNotifications(String authSubject,
			NotificationPageRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public NotificationSettingsResponse getMySettings(String authSubject) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
