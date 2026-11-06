package com.cusfit.cusfitbe.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.dto.request.SignUpRequest;
import com.cusfit.cusfitbe.user.dto.request.WithdrawRequest;
import com.cusfit.cusfitbe.user.dto.response.NicknameAvailabilityResponse;
import com.cusfit.cusfitbe.user.service.UserReadService;
import com.cusfit.cusfitbe.user.service.UserWriteService;
import java.util.List;
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

@WebMvcTest(controllers = UserApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class UserApiControllerTest {

	private static final String SUBJECT = "firebase-uid-1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserReadService userReadService;

	@MockitoBean
	private UserWriteService userWriteService;

	@Test
	@DisplayName("형식에 맞는 닉네임의 사용 가능 여부를 조회하면 200과 결과를 반환한다")
	void checkNicknameAvailability() throws Exception {
		// given
		given(userReadService.checkNicknameAvailability("온도2")).willReturn(new NicknameAvailabilityResponse(true));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/users/nickname/availability")
				.param("nickname", "온도2")
				.with(jwt()));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.available").value(true));
	}

	@ParameterizedTest
	@ValueSource(strings = {"온", "열세글자가넘는닉네임입니다", "공백 있음", "특수문자!", ""})
	@DisplayName("닉네임 형식이 틀리면 400 INVALID_REQUEST를 반환하고 중복을 확인하지 않는다")
	void checkInvalidNickname(String nickname) throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/users/nickname/availability")
				.param("nickname", nickname)
				.with(jwt()));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
		verify(userReadService, never()).checkNicknameAvailability(anyString());
	}

	@Test
	@DisplayName("회원가입을 요청하면 토큰 subject를 넘기고, 아직 구현되지 않아 501 NOT_IMPLEMENTED를 반환한다")
	void signUpNotImplemented() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("온도", List.of(1L, 2L, 3L));
		given(userWriteService.signUp(SUBJECT, request)).willThrow(new BusinessException(ErrorCode.NOT_IMPLEMENTED));

		// when
		ResultActions result = mockMvc.perform(post("/api/v1/users")
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nickname\": \"온도\", \"agreedTermsIds\": [1, 2, 3]}"));

		// then
		result.andExpect(status().isNotImplemented())
				.andExpect(jsonPath("$.error.code").value("NOT_IMPLEMENTED"));
	}

	@Test
	@DisplayName("동의한 약관 없이 회원가입하면 400 INVALID_REQUEST를 반환한다")
	void signUpWithoutAgreedTerms() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post("/api/v1/users")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nickname\": \"온도\", \"agreedTermsIds\": []}"));

		// then
		result.andExpect(status().isBadRequest());
		verify(userWriteService, never()).signUp(anyString(), any());
	}

	@Test
	@DisplayName("탈퇴 동의 없이 탈퇴하면 400 INVALID_REQUEST를 반환하고 탈퇴하지 않는다")
	void withdrawWithoutAgreement() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/users/me")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"agreed\": false}"));

		// then
		result.andExpect(status().isBadRequest());
		verify(userWriteService, never()).withdraw(anyString(), any());
	}

	@Test
	@DisplayName("탈퇴에 동의하고 탈퇴하면 204를 반환한다")
	void withdraw() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/users/me")
				.with(jwt().jwt(j -> j.subject(SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"agreed\": true, \"deleteMyRecipes\": true}"));

		// then
		result.andExpect(status().isNoContent());
		verify(userWriteService).withdraw(SUBJECT, new WithdrawRequest(true, true, null));
	}
}
