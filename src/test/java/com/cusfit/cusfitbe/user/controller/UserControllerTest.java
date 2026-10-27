package com.cusfit.cusfitbe.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.SecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.controller.docs.UserControllerDocs;
import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.service.UserReadService;
import com.cusfit.cusfitbe.user.service.UserWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class UserControllerTest {

	private static final String BASE_PATH = "/api/dev/users";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserReadService userReadService;

	@MockitoBean
	private UserWriteService userWriteService;

	@Test
	@DisplayName("유효한 사용자 생성 요청을 보내면 201과 Location, 생성된 사용자를 반환한다")
	void create() throws Exception {
		// given
		UserResponse response = UserResponse.from(UserFixture.persistedUser());
		given(userWriteService.create(any(CreateUserRequest.class))).willReturn(response);

		// when
		ResultActions result = mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(UserFixture.createRequestJson(UserFixture.NICKNAME)));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", BASE_PATH + "/" + UserFixture.ID))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(UserFixture.ID))
				.andExpect(jsonPath("$.data.nickname").value(UserFixture.NICKNAME))
				.andExpect(jsonPath("$.data.createdAt").exists());
		then(userWriteService).should().create(UserFixture.createRequest());
	}

	@Test
	@DisplayName("닉네임이 공백인 생성 요청을 보내면 INVALID_REQUEST 400을 반환하고 서비스를 호출하지 않는다")
	void createWithBlankNickname() throws Exception {
		// given
		String body = UserFixture.createRequestJson(" ");

		// when
		ResultActions result = mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		then(userWriteService).should(never()).create(any());
	}

	@Test
	@DisplayName("닉네임이 누락된 생성 요청을 보내면 INVALID_REQUEST 400을 반환한다")
	void createWithoutNickname() throws Exception {
		// given
		String body = UserFixture.createRequestJson(null);

		// when
		ResultActions result = mockMvc.perform(post(BASE_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		then(userWriteService).should(never()).create(any());
	}

	@Test
	@DisplayName("존재하는 사용자 ID로 조회하면 200과 사용자를 반환한다")
	void getById() throws Exception {
		// given
		UserResponse response = UserResponse.from(UserFixture.persistedUser());
		given(userReadService.getById(UserFixture.ID)).willReturn(response);

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + UserFixture.ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(UserFixture.ID))
				.andExpect(jsonPath("$.data.nickname").value(UserFixture.NICKNAME))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 조회하면 NOT_FOUND 404를 반환한다")
	void getByIdNotFound() throws Exception {
		// given
		Long missingId = 999L;
		given(userReadService.getById(missingId)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + missingId));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("숫자가 아닌 사용자 ID로 조회하면 INVALID_REQUEST 400을 반환한다")
	void getByInvalidId() throws Exception {
		// given
		String path = BASE_PATH + "/not-a-number";

		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("사용자 개발 Controller를 확인하면 local 프로필 전용이고 Docs 인터페이스를 구현한다")
	void localOnlyController() {
		// given
		Class<UserController> type = UserController.class;

		// when
		Profile profile = type.getAnnotation(Profile.class);

		// then
		assertThat(profile).isNotNull();
		assertThat(profile.value()).containsExactly("local");
		assertThat(UserControllerDocs.class).isAssignableFrom(type);
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
