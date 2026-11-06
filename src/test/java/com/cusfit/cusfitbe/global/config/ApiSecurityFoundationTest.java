package com.cusfit.cusfitbe.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영 API(/api/v1)와 local 외 기본 체인을 실제 필터로 검증한다. 스텁 Controller가 모든 경로에 200을 응답하므로 2xx가 아니면 보안
 * 필터에서 막힌 것이다. 이 테스트 프로필에는 JWT Issuer가 없어 실제 토큰은 모두 거부되고, 인증 성공은 jwt() 후처리기로 만든다.
 */
@WebMvcTest(controllers = ApiSecurityFoundationTest.StubController.class)
@ActiveProfiles("test")
@Import({ApiSecurityConfig.class, DefaultSecurityConfig.class, ApiSecurityFoundationTest.StubController.class})
class ApiSecurityFoundationTest {

	private static final String SUBJECT = "firebase-uid-1";

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest
	@ValueSource(strings = {"/api/v1/terms", "/api/v1/terms/1", "/api/v1/brands", "/api/v1/brands/1/option-presets",
			"/api/v1/menus", "/api/v1/notices", "/api/v1/notices/1", "/api/v1/faqs", "/api/v1/recipes",
			"/api/v1/recipes/1"})
	@DisplayName("공개 조회 API는 토큰 없이 GET하면 허용된다")
	void publicGetPermitted(String path) throws Exception {
		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(status().isOk());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/api/v1/users/me", "/api/v1/users/me/recipes", "/api/v1/notifications",
			"/api/v1/users/nickname/availability"})
	@DisplayName("회원 API는 토큰 없이 GET하면 401을 반환한다")
	void protectedGetWithoutToken(String path) throws Exception {
		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("공개 조회 경로라도 토큰 없이 쓰기 요청을 보내면 401을 반환한다")
	void writeOnPublicPathWithoutToken() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post("/api/v1/recipes"));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("인증된 사용자가 CSRF 토큰 없이 쓰기 요청을 보내면 허용되고 토큰 subject가 전달된다")
	void authenticatedWriteWithoutCsrf() throws Exception {
		// when
		ResultActions result = mockMvc.perform(put("/api/v1/recipes/1/like").with(jwt().jwt(j -> j.subject(SUBJECT))));

		// then
		result.andExpect(status().isOk())
				.andExpect(content().string(SUBJECT));
	}

	@Test
	@DisplayName("ADMIN 권한이 없는 사용자가 관리자 API를 호출하면 403을 반환한다")
	void adminWithoutRole() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post("/api/v1/admin/brands").with(jwt()));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("ADMIN 권한이 있는 사용자가 관리자 API를 호출하면 허용된다")
	void adminWithRole() throws Exception {
		// when
		ResultActions result = mockMvc.perform(post("/api/v1/admin/brands")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("JWT Issuer가 설정되지 않은 환경에서 Bearer 토큰을 보내면 401을 반환한다")
	void bearerTokenRejectedWithoutIssuer() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer any-token"));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/actuator/health", "/swagger-ui.html", "/api/dev/users/1"})
	@DisplayName("local 외 프로필에서 /api/v1 밖의 경로는 차단된다")
	void nonApiPathsDenied(String path) throws Exception {
		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(denied());
	}

	private static ResultMatcher denied() {
		return mvcResult -> assertThat(mvcResult.getResponse().getStatus()).isIn(401, 403);
	}

	@RestController
	public static class StubController {

		@GetMapping({"/api/v1/terms", "/api/v1/terms/1", "/api/v1/brands", "/api/v1/brands/1/option-presets",
				"/api/v1/menus", "/api/v1/notices", "/api/v1/notices/1", "/api/v1/faqs", "/api/v1/recipes",
				"/api/v1/recipes/1", "/api/v1/users/me", "/api/v1/users/me/recipes", "/api/v1/notifications",
				"/api/v1/users/nickname/availability", "/actuator/health", "/swagger-ui.html", "/api/dev/users/1"})
		public String open() {
			return "ok";
		}

		@PostMapping({"/api/v1/recipes", "/api/v1/admin/brands"})
		public String write() {
			return "ok";
		}

		@PutMapping("/api/v1/recipes/1/like")
		public String subject(@AuthenticationPrincipal Jwt jwt) {
			return jwt.getSubject();
		}
	}
}
