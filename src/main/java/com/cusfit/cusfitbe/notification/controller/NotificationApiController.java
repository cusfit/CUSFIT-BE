package com.cusfit.cusfitbe.notification.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.notification.controller.docs.NotificationApiControllerDocs;
import com.cusfit.cusfitbe.notification.dto.request.DeleteDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.NotificationPageRequest;
import com.cusfit.cusfitbe.notification.dto.request.RegisterDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.UpdateNotificationSettingsRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationItemResponse;
import com.cusfit.cusfitbe.notification.dto.response.NotificationSettingsResponse;
import com.cusfit.cusfitbe.notification.service.NotificationReadService;
import com.cusfit.cusfitbe.notification.service.NotificationWriteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class NotificationApiController implements NotificationApiControllerDocs {

	private final NotificationReadService readService;
	private final NotificationWriteService writeService;

	public NotificationApiController(NotificationReadService readService, NotificationWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@GetMapping("/notifications")
	public ResponseEntity<ApiResponse<PageResponse<NotificationItemResponse>>> getMyNotifications(
			@AuthenticationPrincipal Jwt jwt, @Valid @ModelAttribute NotificationPageRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getMyNotifications(jwt.getSubject(), request)));
	}

	@Override
	@GetMapping("/users/me/notification-settings")
	public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getMySettings(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getMySettings(jwt.getSubject())));
	}

	@Override
	@PutMapping("/users/me/notification-settings")
	public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateMySettings(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateNotificationSettingsRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.updateMySettings(jwt.getSubject(), request)));
	}

	@Override
	@PutMapping("/users/me/device-tokens")
	public ResponseEntity<Void> registerDeviceToken(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody RegisterDeviceTokenRequest request) {
		writeService.registerDeviceToken(jwt.getSubject(), request);
		return ResponseEntity.noContent().build();
	}

	@Override
	@DeleteMapping("/users/me/device-tokens")
	public ResponseEntity<Void> deleteDeviceToken(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody DeleteDeviceTokenRequest request) {
		writeService.deleteDeviceToken(jwt.getSubject(), request);
		return ResponseEntity.noContent().build();
	}
}
