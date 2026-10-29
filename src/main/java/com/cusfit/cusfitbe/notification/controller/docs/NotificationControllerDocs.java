package com.cusfit.cusfitbe.notification.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface NotificationControllerDocs {

	@Operation(summary = "Create Notification")
	ResponseEntity<ApiResponse<NotificationResponse>> create(CreateNotificationRequest request);

	@Operation(summary = "Get Notification")
	ResponseEntity<ApiResponse<NotificationResponse>> getById(Long id);
}
