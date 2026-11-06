package com.cusfit.cusfitbe.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.notification.dto.request.DeleteDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.dto.request.NotificationPageRequest;
import com.cusfit.cusfitbe.notification.dto.request.RegisterDeviceTokenRequest;
import com.cusfit.cusfitbe.notification.entity.DevicePlatform;
import com.cusfit.cusfitbe.notification.entity.NotificationCategory;
import com.cusfit.cusfitbe.notification.service.NotificationReadService;
import com.cusfit.cusfitbe.notification.service.NotificationWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = NotificationApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class NotificationApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationReadService notificationReadService;

	@MockitoBean
	private NotificationWriteService notificationWriteService;

	@Test
	@DisplayName("크기 없이 알림 목록을 조회하면 기본 크기 20으로 서비스에 넘긴다")
	void getNotificationsWithDefaultSize() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notifications")
				.param("tab", "ACTIVITY")
				.with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk());
		verify(notificationReadService).getMyNotifications(SUBJECT,
				new NotificationPageRequest(NotificationCategory.ACTIVITY, null, 20));
	}

	@Test
	@DisplayName("알림 목록 크기가 50을 넘으면 400 INVALID_REQUEST를 반환한다")
	void getNotificationsWithTooLargeSize() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notifications").param("size", "51").with(jwt()));

		// then
		result.andExpect(status().isBadRequest());
		verify(notificationReadService, never()).getMyNotifications(anyString(), any());
	}

	@Test
	@DisplayName("알림 설정 항목이 빠지면 400 INVALID_REQUEST를 반환하고 변경하지 않는다")
	void updateSettingsWithMissingField() throws Exception {
		// when
		ResultActions result = mockMvc.perform(put("/api/v1/users/me/notification-settings")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"activity\": true, \"news\": false}"));

		// then
		result.andExpect(status().isBadRequest());
		verify(notificationWriteService, never()).updateMySettings(anyString(), any());
	}

	@Test
	@DisplayName("기기 토큰을 등록하면 204를 반환하고 토큰과 플랫폼을 서비스에 넘긴다")
	void registerDeviceToken() throws Exception {
		// when
		ResultActions result = mockMvc.perform(put("/api/v1/users/me/device-tokens")
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\": \"fcm-token\", \"platform\": \"IOS\"}"));

		// then
		result.andExpect(status().isNoContent());
		verify(notificationWriteService).registerDeviceToken(SUBJECT,
				new RegisterDeviceTokenRequest("fcm-token", DevicePlatform.IOS));
	}

	@Test
	@DisplayName("기기 토큰을 삭제하면 204를 반환하고 그 토큰만 서비스에 넘긴다")
	void deleteDeviceToken() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/users/me/device-tokens")
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\": \"fcm-token\"}"));

		// then
		result.andExpect(status().isNoContent());
		verify(notificationWriteService).deleteDeviceToken(SUBJECT, new DeleteDeviceTokenRequest("fcm-token"));
	}
}
