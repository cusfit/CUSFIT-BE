package com.cusfit.cusfitbe.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.fixture.FoundationRequestFixture;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 보안 필터와 분리해 공통 예외 처리만 검증한다. 스텁 Controller는 테스트 내부 클래스라 컴포넌트 스캔 대상이 아니며
 * {@link Import}로만 등록된다.
 */
@WebMvcTest(controllers = MalformedRequestFoundationTest.StubController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MalformedRequestFoundationTest.StubController.class)
class MalformedRequestFoundationTest {

	private static final String STUB_PATH = "/api/dev/foundation-stubs";

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("올바른 JSON 요청을 보내면 200과 성공 응답을 반환한다")
	void validRequest() throws Exception {
		// given
		String body = FoundationRequestFixture.validJson();

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value(FoundationRequestFixture.VALID_NAME));
	}

	@Test
	@DisplayName("문법이 깨진 JSON을 보내면 INVALID_REQUEST 400을 반환한다")
	void malformedJson() throws Exception {
		// given
		String body = FoundationRequestFixture.malformedJson();

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("요청 본문 없이 보내면 INVALID_REQUEST 400을 반환한다")
	void missingBody() throws Exception {
		// given
		MediaType contentType = MediaType.APPLICATION_JSON;

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH).contentType(contentType));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("JSON 본문의 날짜 형식이 잘못되면 INVALID_REQUEST 400을 반환한다")
	void invalidInstantInBody() throws Exception {
		// given
		String body = FoundationRequestFixture.invalidInstantJson();

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("JSON 본문의 ID 타입이 잘못되면 INVALID_REQUEST 400을 반환한다")
	void invalidIdInBody() throws Exception {
		// given
		String body = FoundationRequestFixture.invalidIdJson();

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("경로 변수 ID 타입이 잘못되면 INVALID_REQUEST 400을 반환한다")
	void invalidPathVariableId() throws Exception {
		// given
		String path = STUB_PATH + "/not-a-number";

		// when
		ResultActions result = mockMvc.perform(get(path));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("요청 파라미터의 날짜 형식이 잘못되면 INVALID_REQUEST 400을 반환한다")
	void invalidInstantRequestParam() throws Exception {
		// given
		String occurredAt = "not-a-date";

		// when
		ResultActions result = mockMvc.perform(get(STUB_PATH).param("occurredAt", occurredAt));

		// then
		expectInvalidRequest(result);
	}

	@Test
	@DisplayName("필수 문자열이 공백이면 기존과 같이 INVALID_REQUEST 400을 반환한다")
	void blankRequiredText() throws Exception {
		// given
		String body = FoundationRequestFixture.blankNameJson();

		// when
		ResultActions result = mockMvc.perform(post(STUB_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

		// then
		expectInvalidRequest(result);
	}

	private static void expectInvalidRequest(ResultActions result) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_REQUEST.name()));
	}

	@RestController
	@RequestMapping(STUB_PATH)
	public static class StubController {

		@PostMapping
		public ApiResponse<StubResponse> create(@Valid @RequestBody StubRequest request) {
			return ApiResponse.ok(new StubResponse(request.name(), request.targetId(), request.occurredAt()));
		}

		@GetMapping("/{id}")
		public ApiResponse<Long> getById(@PathVariable("id") Long id) {
			return ApiResponse.ok(id);
		}

		@GetMapping
		public ApiResponse<Instant> search(@RequestParam("occurredAt") Instant occurredAt) {
			return ApiResponse.ok(occurredAt);
		}
	}

	public record StubRequest(@NotBlank String name, Long targetId, Instant occurredAt) {
	}

	public record StubResponse(String name, Long targetId, Instant occurredAt) {
	}
}
