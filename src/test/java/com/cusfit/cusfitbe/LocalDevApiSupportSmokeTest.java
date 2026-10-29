package com.cusfit.cusfitbe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

/**
 * local 프로필에서 MockMvc HTTP 요청 → 실제 Controller/Service/Repository → Testcontainers DB로 모든 개발 리소스의
 * 생성·단건 조회를 연결해 확인한다. 트랜잭션 롤백 없이 실제로 커밋한 뒤 별도 요청으로 재조회한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Import(PostgisContainerConfig.class)
class LocalDevApiSupportSmokeTest {

	private static final List<String> DEV_RESOURCES = List.of("users", "terms", "brands", "menus", "option-presets",
			"recipes", "reports", "notifications", "notices", "faqs", "inquiries");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("local에서 모든 개발 리소스를 POST로 생성하고 Location으로 GET하면 실제 DB에 저장된 값이 조회된다")
	void createAndGetAllResources() throws Exception {
		// given
		Map<String, Object> userBody = Map.of("nickname", "스모크유저");

		// when & then
		Long userId = createAndVerify("users", userBody);
		createAndVerify("terms", Map.of("type", "SERVICE", "version", "1.0",
				"contentUrl", "https://cusfit.example/terms/1.0", "effectiveAt", "2026-01-01T00:00:00Z"));
		Long brandId = createAndVerify("brands", Map.of("name", "스모크브랜드"));
		Long menuId = createAndVerify("menus", Map.of("brandId", brandId, "name", "아이스 아메리카노"));
		createAndVerify("option-presets", Map.of("brandId", brandId, "name", "샷 추가"));
		Long recipeId = createAndVerify("recipes", Map.of("authorId", userId, "menuId", menuId,
				"title", "빈 옵션 레시피", "options", List.of()));
		createAndVerify("reports", Map.of("reporterId", userId, "recipeId", recipeId, "content", "부적절한 레시피"));
		createAndVerify("notifications", Map.of("recipientId", userId, "title", "새 레시피", "content", "등록되었습니다"));
		createAndVerify("notices", Map.of("title", "점검 안내", "content", "새벽 점검이 있습니다"));
		createAndVerify("faqs", Map.of("question", "레시피 등록 방법은?", "answer", "메뉴를 고르면 됩니다"));
		createAndVerify("inquiries", Map.of("authorId", userId, "title", "문의", "content", "저장이 안 됩니다"));
	}

	@Test
	@DisplayName("local에서 옵션이 있는 레시피를 생성하고 재조회하면 옵션 ID, 생성 시각, 입력 순서가 유지된다")
	void recipeOptionsSurviveRoundTrip() throws Exception {
		// given
		Long userId = createAndVerify("users", Map.of("nickname", "옵션유저"));
		Long brandId = createAndVerify("brands", Map.of("name", "옵션브랜드"));
		Long menuId = createAndVerify("menus", Map.of("brandId", brandId, "name", "바닐라 라떼"));
		Map<String, Object> recipeBody = Map.of("authorId", userId, "menuId", menuId, "title", "옵션 레시피",
				"options", List.of(Map.of("name", "샷 추가"), Map.of("name", "시럽 제외"), Map.of("name", "얼음 적게")));

		// when
		Map<String, Object> created = postCreated("recipes", recipeBody);
		Map<String, Object> found = getById("recipes", id(created));

		// then
		assertThat(found).containsAllEntriesOf(expected(recipeBody, id(created)));
		List<Map<String, Object>> createdOptions = options(created);
		List<Map<String, Object>> foundOptions = options(found);
		assertThat(foundOptions).extracting(option -> option.get("name"))
				.containsExactly("샷 추가", "시럽 제외", "얼음 적게");
		assertThat(foundOptions).extracting(option -> option.get("position")).containsExactly(0, 1, 2);
		assertThat(foundOptions).extracting(option -> option.get("id"))
				.doesNotContainNull()
				.doesNotHaveDuplicates()
				.containsExactlyElementsOf(createdOptions.stream().map(option -> option.get("id")).toList());
		assertThat(foundOptions).allSatisfy(option -> assertThat(createdAt(option)).isNotNull());
	}

	@Test
	@DisplayName("local에서 /api-docs를 조회하면 200과 함께 모든 개발 API 경로가 문서에 등록되어 있다")
	void apiDocsContainsDevPaths() throws Exception {
		// given
		String docsPath = "/api-docs";

		// when
		ResultActions result = mockMvc.perform(get(docsPath));

		// then
		result.andExpect(status().isOk());
		for (String resource : DEV_RESOURCES) {
			result.andExpect(jsonPath("$.paths['/api/dev/" + resource + "'].post").exists())
					.andExpect(jsonPath("$.paths['/api/dev/" + resource + "/{id}'].get").exists());
		}
	}

	private Long createAndVerify(String resource, Map<String, Object> body) throws Exception {
		Map<String, Object> created = postCreated(resource, body);
		Long id = id(created);
		Map<String, Object> found = getById(resource, id);
		assertThat(found).containsAllEntriesOf(expected(body, id));
		assertThat(createdAt(found)).isNotNull();
		return id;
	}

	private Map<String, Object> postCreated(String resource, Map<String, Object> body) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/dev/" + resource)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andReturn();
		Map<String, Object> data = data(result);
		assertThat(result.getResponse().getHeader("Location")).isEqualTo("/api/dev/" + resource + "/" + id(data));
		return data;
	}

	private Map<String, Object> getById(String resource, Long id) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/dev/" + resource + "/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andReturn();
		return data(result);
	}

	/** 요청 본문을 JSON 왕복으로 정규화하고 id를 더한다. options는 응답 모양이 달라 따로 검증한다. */
	private Map<String, Object> expected(Map<String, Object> body, Long id) throws Exception {
		Map<String, Object> expected = new HashMap<>(objectMapper.readValue(objectMapper.writeValueAsString(body),
				new TypeReference<Map<String, Object>>() {
				}));
		expected.remove("options");
		expected.put("id", objectMapper.readValue(String.valueOf(id), Object.class));
		return expected;
	}

	private Map<String, Object> data(MvcResult result) throws Exception {
		Map<String, Object> body = objectMapper.readValue(
				result.getResponse().getContentAsString(StandardCharsets.UTF_8),
				new TypeReference<Map<String, Object>>() {
				});
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		return data;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> options(Map<String, Object> recipe) {
		return (List<Map<String, Object>>) recipe.get("options");
	}

	private static Instant createdAt(Map<String, Object> data) {
		Object value = data.get("createdAt");
		return value == null ? null : Instant.parse((String) value);
	}

	private static Long id(Map<String, Object> data) {
		return ((Number) data.get("id")).longValue();
	}
}
