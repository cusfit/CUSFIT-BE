package com.cusfit.cusfitbe.notification.service;

import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;

public interface NotificationReadService {

	NotificationResponse getById(Long id);
}
