package com.cusfit.cusfitbe.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.fixture.FoundationRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * local 프로필의 SecurityFilterChain을 실제 필터로 검증한다. 스텁 Controller가 허용/차단 대상 경로를 모두 200으로 응답하므로,
 * 2xx가 아니면 보안 필터에서 막힌 것이다.
 */
@WebMvcTest(controllers = LocalSecurityFoundationTest.StubController.class)
@ActiveProfiles("local")
@Import({SecurityConfig.class, LocalSecurityFoundationTest.StubController.class})
class LocalSecurityFoundationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("local에서 인증과 CSRF 토큰 없이 개발 API에 POST하면 허용된다")
	void devPostPermittedWithoutAuthAndCsrf() throws Exception {
		// given
		String body = FoundationRequestFixture.validJson();

		// when
		ResultActions result = mockMvc.perform(post("/api/dev/foundation-stubs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("local에서 인증 없이 개발 API를 GET하면 허용된다")
	void devGetPermittedWithoutAuth() throws Exception {
		// given
		String path = "/api/dev/foundation-stubs/1";

		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(status().isOk());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/swagger-ui.html", "/swagger-ui/index.html", "/api-docs", "/api-docs/swagger-config",
			"/actuator/health"})
	@DisplayName("local에서 인증 없이 Swagger 문서 경로나 health를 GET하면 허용된다")
	void docsAndHealthPermitted(String path) throws Exception {
		// given
		String target = path;

		// when
		ResultActions result = mockMvc.perform(get(target));

		// then
		result.andExpect(status().isOk());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/api/users/1", "/api/development", "/actuator/prometheus", "/"})
	@DisplayName("local에서 허용 목록 밖의 경로를 GET하면 차단된다")
	void otherPathsDenied(String path) throws Exception {
		// given
		String target = path;

		// when
		ResultActions result = mockMvc.perform(get(target));

		// then
		result.andExpect(denied());
	}

	@Test
	@DisplayName("local에서 허용 목록 밖의 경로에 POST하면 차단된다")
	void otherPathPostDenied() throws Exception {
		// given
		String body = FoundationRequestFixture.validJson();

		// when
		ResultActions result = mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		result.andExpect(denied());
	}

	private static ResultMatcher denied() {
		return mvcResult -> assertThat(mvcResult.getResponse().getStatus()).isIn(401, 403);
	}

	@RestController
	public static class StubController {

		@PostMapping({"/api/dev/foundation-stubs", "/api/users"})
		public String create(@RequestBody String body) {
			return "created";
		}

		@GetMapping("/api/dev/foundation-stubs/{id}")
		public String getById(@PathVariable("id") Long id) {
			return "found";
		}

		@GetMapping({"/swagger-ui.html", "/swagger-ui/index.html", "/api-docs", "/api-docs/swagger-config",
				"/actuator/health", "/actuator/prometheus", "/api/users/1", "/api/development", "/"})
		public String open() {
			return "ok";
		}
	}
}
