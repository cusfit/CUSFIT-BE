package com.cusfit.cusfitbe.catalog.controller;

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

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.BrandWriteService;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.catalog.service.MenuWriteService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetWriteService;
import com.cusfit.cusfitbe.global.config.SecurityConfig;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
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

@WebMvcTest(controllers = {BrandController.class, MenuController.class, OptionPresetController.class})
@ActiveProfiles("local")
@Import(SecurityConfig.class)
class CatalogControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BrandReadService brandReadService;

	@MockitoBean
	private BrandWriteService brandWriteService;

	@MockitoBean
	private MenuReadService menuReadService;

	@MockitoBean
	private MenuWriteService menuWriteService;

	@MockitoBean
	private OptionPresetReadService optionPresetReadService;

	@MockitoBean
	private OptionPresetWriteService optionPresetWriteService;

	@Test
	@DisplayName("브랜드를 생성하면 201과 Location, 생성된 브랜드를 반환한다")
	void createBrand() throws Exception {
		// given
		given(brandWriteService.create(any(CreateBrandRequest.class))).willReturn(CatalogFixture.brandResponse());

		// when
		ResultActions result = postJson("/api/dev/brands", CatalogFixture.brandJson(CatalogFixture.BRAND_NAME));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/dev/brands/" + CatalogFixture.BRAND_ID)))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.BRAND_ID))
				.andExpect(jsonPath("$.data.name").value(CatalogFixture.BRAND_NAME))
				.andExpect(jsonPath("$.data.createdAt").exists());
	}

	@Test
	@DisplayName("브랜드 이름이 공백이면 400 INVALID_REQUEST를 반환하고 생성하지 않는다")
	void createBrandBlankName() throws Exception {
		// given
		String body = CatalogFixture.brandJson(" ");

		// when
		ResultActions result = postJson("/api/dev/brands", body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(brandWriteService, never()).create(any());
	}

	@Test
	@DisplayName("브랜드를 단건 조회하면 200과 브랜드를 반환한다")
	void getBrand() throws Exception {
		// given
		given(brandReadService.getById(CatalogFixture.BRAND_ID)).willReturn(CatalogFixture.brandResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/brands/{id}", CatalogFixture.BRAND_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.BRAND_ID))
				.andExpect(jsonPath("$.data.name").value(CatalogFixture.BRAND_NAME));
	}

	@Test
	@DisplayName("없는 브랜드를 조회하면 404 NOT_FOUND를 반환한다")
	void getBrandNotFound() throws Exception {
		// given
		given(brandReadService.getById(CatalogFixture.BRAND_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/brands/{id}", CatalogFixture.BRAND_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("메뉴를 생성하면 201과 Location, 생성된 메뉴를 반환한다")
	void createMenu() throws Exception {
		// given
		given(menuWriteService.create(any(CreateMenuRequest.class))).willReturn(CatalogFixture.menuResponse());

		// when
		ResultActions result = postJson("/api/dev/menus",
				CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, CatalogFixture.MENU_NAME));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/dev/menus/" + CatalogFixture.MENU_ID)))
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.MENU_ID))
				.andExpect(jsonPath("$.data.brandId").value(CatalogFixture.BRAND_ID))
				.andExpect(jsonPath("$.data.name").value(CatalogFixture.MENU_NAME));
	}

	@Test
	@DisplayName("메뉴 생성 요청에 브랜드 ID가 없으면 400 INVALID_REQUEST를 반환한다")
	void createMenuWithoutBrandId() throws Exception {
		// given
		String body = CatalogFixture.brandScopedJson(null, CatalogFixture.MENU_NAME);

		// when
		ResultActions result = postJson("/api/dev/menus", body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(menuWriteService, never()).create(any());
	}

	@Test
	@DisplayName("메뉴 이름이 공백이면 400 INVALID_REQUEST를 반환한다")
	void createMenuBlankName() throws Exception {
		// given
		String body = CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, " ");

		// when
		ResultActions result = postJson("/api/dev/menus", body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(menuWriteService, never()).create(any());
	}

	@Test
	@DisplayName("없는 브랜드로 메뉴를 생성하면 404 NOT_FOUND를 반환한다")
	void createMenuWithMissingBrand() throws Exception {
		// given
		given(menuWriteService.create(any(CreateMenuRequest.class)))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = postJson("/api/dev/menus",
				CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, CatalogFixture.MENU_NAME));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("메뉴를 단건 조회하면 200과 메뉴를 반환한다")
	void getMenu() throws Exception {
		// given
		given(menuReadService.getById(CatalogFixture.MENU_ID)).willReturn(CatalogFixture.menuResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/menus/{id}", CatalogFixture.MENU_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.MENU_ID))
				.andExpect(jsonPath("$.data.brandId").value(CatalogFixture.BRAND_ID));
	}

	@Test
	@DisplayName("없는 메뉴를 조회하면 404 NOT_FOUND를 반환한다")
	void getMenuNotFound() throws Exception {
		// given
		given(menuReadService.getById(CatalogFixture.MENU_ID)).willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/menus/{id}", CatalogFixture.MENU_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("옵션 프리셋을 생성하면 201과 Location, 생성된 옵션 프리셋을 반환한다")
	void createOptionPreset() throws Exception {
		// given
		given(optionPresetWriteService.create(any(CreateOptionPresetRequest.class)))
				.willReturn(CatalogFixture.optionPresetResponse());

		// when
		ResultActions result = postJson("/api/dev/option-presets",
				CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, CatalogFixture.OPTION_PRESET_NAME));

		// then
		result.andExpect(status().isCreated())
				.andExpect(header().string("Location",
						endsWith("/api/dev/option-presets/" + CatalogFixture.OPTION_PRESET_ID)))
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.OPTION_PRESET_ID))
				.andExpect(jsonPath("$.data.brandId").value(CatalogFixture.BRAND_ID))
				.andExpect(jsonPath("$.data.name").value(CatalogFixture.OPTION_PRESET_NAME));
	}

	@Test
	@DisplayName("옵션 프리셋 생성 요청에 브랜드 ID가 없으면 400 INVALID_REQUEST를 반환한다")
	void createOptionPresetWithoutBrandId() throws Exception {
		// given
		String body = CatalogFixture.brandScopedJson(null, CatalogFixture.OPTION_PRESET_NAME);

		// when
		ResultActions result = postJson("/api/dev/option-presets", body);

		// then
		expectError(result, 400, ErrorCode.INVALID_REQUEST);
		verify(optionPresetWriteService, never()).create(any());
	}

	@Test
	@DisplayName("옵션 프리셋을 단건 조회하면 200과 옵션 프리셋을 반환한다")
	void getOptionPreset() throws Exception {
		// given
		given(optionPresetReadService.getById(CatalogFixture.OPTION_PRESET_ID))
				.willReturn(CatalogFixture.optionPresetResponse());

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/option-presets/{id}", CatalogFixture.OPTION_PRESET_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.OPTION_PRESET_ID))
				.andExpect(jsonPath("$.data.name").value(CatalogFixture.OPTION_PRESET_NAME));
	}

	@Test
	@DisplayName("없는 옵션 프리셋을 조회하면 404 NOT_FOUND를 반환한다")
	void getOptionPresetNotFound() throws Exception {
		// given
		given(optionPresetReadService.getById(CatalogFixture.OPTION_PRESET_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		ResultActions result = mockMvc.perform(get("/api/dev/option-presets/{id}", CatalogFixture.OPTION_PRESET_ID));

		// then
		expectError(result, 404, ErrorCode.NOT_FOUND);
	}

	private ResultActions postJson(String path, String body) throws Exception {
		return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static void expectError(ResultActions result, int status, ErrorCode code) throws Exception {
		result.andExpect(status().is(status))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value(code.name()));
	}
}
