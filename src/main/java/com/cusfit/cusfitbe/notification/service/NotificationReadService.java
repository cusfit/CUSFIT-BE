package com.cusfit.cusfitbe.notification.service;

import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.notification.dto.request.NotificationPageRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationItemResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;

public interface NotificationReadService {

	NotificationResponse getById(Long id);

	PageResponse<NotificationItemResponse> getMyNotifications(String authSubject, NotificationPageRequest request);

	NotificationSettingsResponse getMySettings(String authSubject);
}
