package com.cusfit.cusfitbe.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.cusfit.cusfitbe.global.fixture.FoundationRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

/** local 외 프로필(test)에서는 개발 API에 permitAll이 적용되지 않는지 실제 필터로 검증한다. */
@WebMvcTest(controllers = NonLocalSecurityFoundationTest.StubController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, NonLocalSecurityFoundationTest.StubController.class})
class NonLocalSecurityFoundationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("local이 아닌 프로필에서 인증 없이 개발 API에 POST하면 허용되지 않는다")
	void devPostNotPermitted() throws Exception {
		// given
		String body = FoundationRequestFixture.validJson();

		// when
		ResultActions result = mockMvc.perform(post("/api/dev/foundation-stubs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		result.andExpect(notSuccessful());
	}

	@Test
	@DisplayName("local이 아닌 프로필에서 인증 없이 개발 API를 GET하면 허용되지 않는다")
	void devGetNotPermitted() throws Exception {
		// given
		String path = "/api/dev/foundation-stubs/1";

		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		result.andExpect(notSuccessful());
	}

	private static ResultMatcher notSuccessful() {
		return mvcResult -> assertThat(mvcResult.getResponse().getStatus() / 100).isNotEqualTo(2);
	}

	@RestController
	public static class StubController {

		@PostMapping("/api/dev/foundation-stubs")
		public String create(@RequestBody String body) {
			return "created";
		}

		@GetMapping("/api/dev/foundation-stubs/{id}")
		public String getById(@PathVariable("id") Long id) {
			return "found";
		}
	}
}
