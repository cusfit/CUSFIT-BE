package com.cusfit.cusfitbe.notification.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.notification.dto.request.DeleteDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.NotificationPageRequest;
import com.cusfit.cusfitbe.notification.dto.request.RegisterDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.UpdateNotificationSettingsRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationItemResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Notification")
public interface NotificationApiControllerDocs {

	@Operation(summary = "N1. 알림 목록 (미구현)", description = "커서 방식. tab이 없으면 전체.")
	ResponseEntity<ApiResponse<PageResponse<NotificationItemResponse>>> getMyNotifications(Jwt jwt,
			NotificationPageRequest request);

	@Operation(summary = "N2. 알림 설정 조회 (미구현)")
	ResponseEntity<ApiResponse<NotificationSettingsResponse>> getMySettings(Jwt jwt);

	@Operation(summary = "N2. 알림 설정 변경 (미구현)")
	ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateMySettings(Jwt jwt,
			UpdateNotificationSettingsRequest request);

	@Operation(summary = "N3. 기기 토큰 등록 (미구현)")
	ResponseEntity<Void> registerDeviceToken(Jwt jwt, RegisterDeviceTokenRequest request);

	@Operation(summary = "N3. 기기 토큰 삭제 (미구현)", description = "로그아웃 때 이 기기의 토큰만 지운다.")
	ResponseEntity<Void> deleteDeviceToken(Jwt jwt, DeleteDeviceTokenRequest request);
}
