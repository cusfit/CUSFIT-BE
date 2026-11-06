package com.cusfit.cusfitbe.notification.service;

import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.request.DeleteDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.RegisterDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.UpdateNotificationSettingsRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;

public interface NotificationWriteService {

	NotificationResponse create(CreateNotificationRequest request);

	NotificationSettingsResponse updateMySettings(String authSubject, UpdateNotificationSettingsRequest request);

	void registerDeviceToken(String authSubject, RegisterDeviceTokenRequest request);

	void deleteDeviceToken(String authSubject, DeleteDeviceTokenRequest request);
}
