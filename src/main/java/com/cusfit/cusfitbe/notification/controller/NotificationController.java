package com.cusfit.cusfitbe.notification.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.notification.controller.docs.NotificationControllerDocs;
import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.dto.response.NotificationResponse;
import com.cusfit.cusfitbe.notification.service.NotificationReadService;
import com.cusfit.cusfitbe.notification.service.NotificationWriteService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/api/dev/notifications")
public class NotificationController implements NotificationControllerDocs {

	private final NotificationReadService readService;
	private final NotificationWriteService writeService;

	public NotificationController(NotificationReadService readService, NotificationWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<NotificationResponse>> create(
			@Valid @RequestBody CreateNotificationRequest request) {
		NotificationResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/notifications/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
