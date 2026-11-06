package com.cusfit.cusfitbe.catalog.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = CatalogApiController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class CatalogApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BrandReadService brandReadService;

	@MockitoBean
	private MenuReadService menuReadService;

	@MockitoBean
	private OptionPresetReadService optionPresetReadService;

	@Test
	@DisplayName("브랜드 목록을 토큰 없이 조회하면 200과 브랜드 목록을 반환한다")
	void getBrands() throws Exception {
		// given
		given(brandReadService.getAll()).willReturn(List.of(CatalogFixture.brandResponse()));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/brands"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].id").value(CatalogFixture.BRAND_ID))
				.andExpect(jsonPath("$.data[0].name").value(CatalogFixture.BRAND_NAME));
	}

	@Test
	@DisplayName("브랜드 ID와 keyword로 메뉴를 검색하면 두 조건을 서비스에 넘기고 브랜드명을 포함한 메뉴를 반환한다")
	void searchMenus() throws Exception {
		// given
		MenuSummaryResponse menu = new MenuSummaryResponse(CatalogFixture.MENU_ID, CatalogFixture.BRAND_ID,
				CatalogFixture.BRAND_NAME, CatalogFixture.MENU_NAME, CatalogFixture.CREATED_AT);
		given(menuReadService.search(CatalogFixture.BRAND_ID, "아메리카노")).willReturn(List.of(menu));

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/menus")
				.param("brandId", String.valueOf(CatalogFixture.BRAND_ID))
				.param("keyword", "아메리카노"));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].brandName").value(CatalogFixture.BRAND_NAME))
				.andExpect(jsonPath("$.data[0].name").value(CatalogFixture.MENU_NAME));
	}

	@Test
	@DisplayName("조건 없이 메뉴를 검색하면 브랜드 ID와 keyword를 null로 넘긴다")
	void searchMenusWithoutCondition() throws Exception {
		// when
		mockMvc.perform(get("/api/v1/menus")).andExpect(status().isOk());

		// then
		verify(menuReadService).search(null, null);
	}

	@Test
	@DisplayName("브랜드의 옵션 프리셋을 조회하면 200과 프리셋 목록을 반환한다")
	void getOptionPresets() throws Exception {
		// given
		given(optionPresetReadService.getByBrandId(CatalogFixture.BRAND_ID))
				.willReturn(List.of(CatalogFixture.optionPresetResponse()));

		// when
		ResultActions result = mockMvc.perform(
				get("/api/v1/brands/{brandId}/option-presets", CatalogFixture.BRAND_ID));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].name").value(CatalogFixture.OPTION_PRESET_NAME));
	}
}
