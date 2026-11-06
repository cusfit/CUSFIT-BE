package com.cusfit.cusfitbe.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.service.BrandWriteService;
import com.cusfit.cusfitbe.catalog.service.MenuWriteService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetWriteService;
import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = CatalogAdminController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class CatalogAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BrandWriteService brandWriteService;

	@MockitoBean
	private MenuWriteService menuWriteService;

	@MockitoBean
	private OptionPresetWriteService optionPresetWriteService;

	@Test
	@DisplayName("관리자가 브랜드를 등록하면 201과 등록된 브랜드를 반환한다")
	void createBrand() throws Exception {
		// given
		given(brandWriteService.create(CatalogFixture.createBrandRequest())).willReturn(CatalogFixture.brandResponse());

		// when
		ResultActions result = postAsAdmin("/api/v1/admin/brands", CatalogFixture.brandJson(CatalogFixture.BRAND_NAME));

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.BRAND_ID));
	}

	@Test
	@DisplayName("관리자가 메뉴를 등록하면 201과 등록된 메뉴를 반환한다")
	void createMenu() throws Exception {
		// given
		given(menuWriteService.create(CatalogFixture.createMenuRequest(CatalogFixture.BRAND_ID)))
				.willReturn(CatalogFixture.menuResponse());

		// when
		ResultActions result = postAsAdmin("/api/v1/admin/menus",
				CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, CatalogFixture.MENU_NAME));

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(CatalogFixture.MENU_ID));
	}

	@Test
	@DisplayName("옵션 프리셋 이름이 공백이면 400 INVALID_REQUEST를 반환하고 등록하지 않는다")
	void createOptionPresetWithBlankName() throws Exception {
		// when
		ResultActions result = postAsAdmin("/api/v1/admin/option-presets",
				CatalogFixture.brandScopedJson(CatalogFixture.BRAND_ID, " "));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
		verify(optionPresetWriteService, never()).create(any());
	}

	private ResultActions postAsAdmin(String path, String body) throws Exception {
		return mockMvc.perform(post(path)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}
}
