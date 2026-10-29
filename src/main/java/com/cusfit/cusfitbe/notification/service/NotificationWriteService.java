package com.cusfit.cusfitbe.notification.service;

import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;

public interface NotificationWriteService {

	NotificationResponse create(CreateNotificationRequest request);
}
