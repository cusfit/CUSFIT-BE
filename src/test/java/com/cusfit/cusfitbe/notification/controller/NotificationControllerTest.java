package com.cusfit.cusfitbe.notification.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.SecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.notification.dto.request.CreateNotificationRequest;
import com.cusfit.cusfitbe.notification.fixture.NotificationFixture;
import com.cusfit.cusfitbe.notification.service.NotificationReadService;
import com.cusfit.cusfitbe.notification.service.NotificationWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = NotificationController.class)
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class NotificationControllerTest {

	private static final String BASE_PATH = "/api/dev/notifications";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationReadService notificationReadService;

	@MockitoBean
	private NotificationWriteService notificationWriteService;

	@Test
	@DisplayName("알림을 생성하면 201과 Location, 생성된 알림을 반환한다")
	void create() throws Exception {
		// given
		given(notificationWriteService.create(any(CreateNotificationRequest.class)))
				.willReturn(NotificationFixture.response());

		// when
		ResultActions result = postJson(NotificationFixture.createRequestJson(
				NotificationFixture.RECIPIENT_ID, NotificationFixture.TITLE, NotificationFixture.CONTENT));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith(BASE_PATH + "/" + NotificationFixture.ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(NotificationFixture.ID))
				.andExpect(jsonPath("$.data.recipientId").value(NotificationFixture.RECIPIENT_ID))
				.andExpect(jsonPath("$.data.title").value(NotificationFixture.TITLE))
				.andExpect(jsonPath("$.data.content").value(NotificationFixture.CONTENT))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@Test
	@DisplayName("수신자 ID가 없으면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithoutRecipientId() throws Exception {
		// given
		String body = NotificationFixture.createRequestJson(null, NotificationFixture.TITLE,
				NotificationFixture.CONTENT);

		// when
		ResultActions result = postJson(body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(notificationWriteService, never()).create(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {"title", "content"})
	@DisplayName("제목이나 내용이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createWithBlankText(String blankField) throws Exception {
		// given
		String title = blankField.equals("title") ? " " : NotificationFixture.TITLE;
		String content = blankField.equals("content") ? " " : NotificationFixture.CONTENT;
		String body = NotificationFixture.createRequestJson(NotificationFixture.RECIPIENT_ID, title, content);

		// when
		ResultActions result = postJson(body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(notificationWriteService, never()).create(any());
	}

	@Test
	@DisplayName("없는 수신자로 알림을 생성하면 404 NOT_FOUND를 반환한다")
	void createWithMissingRecipient() throws Exception {
		// given
		given(notificationWriteService.create(any(CreateNotificationRequest.class)))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = postJson(NotificationFixture.createRequestJson(
				NotificationFixture.RECIPIENT_ID, NotificationFixture.TITLE, NotificationFixture.CONTENT));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("알림을 단건 조회하면 200과 알림을 반환한다")
	void getById() throws Exception {
		// given
		given(notificationReadService.getById(NotificationFixture.ID)).willReturn(NotificationFixture.response());

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", NotificationFixture.ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(NotificationFixture.ID))
				.andExpect(jsonPath("$.data.recipientId").value(NotificationFixture.RECIPIENT_ID))
				.andExpect(jsonPath("$.data.title").value(NotificationFixture.TITLE));
	}

	@Test
	@DisplayName("없는 알림을 조회하면 404 NOT_FOUND를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		given(notificationReadService.getById(NotificationFixture.ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", NotificationFixture.ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("알림 ID가 숫자가 아니면 400 INVALID_REQUEST를 반환한다")
	void getByInvalidIdType() throws Exception {
		// given
		String invalidId = "abc";

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/{id}", invalidId));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
	}

	private ResultActions postJson(String body) throws Exception {
		return mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
