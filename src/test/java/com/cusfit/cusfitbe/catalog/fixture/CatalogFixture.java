package com.cusfit.cusfitbe.catalog.fixture;

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class CatalogFixture {

	public static final Long BRAND_ID = 1L;
	public static final Long MENU_ID = 10L;
	public static final Long OPTION_PRESET_ID = 20L;
	public static final String BRAND_NAME = "스타벅스";
	public static final String MENU_NAME = "아이스 아메리카노";
	public static final String OPTION_PRESET_NAME = "샷 추가";
	public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	private CatalogFixture() {
	}

	public static Brand brand() {
		return Brand.create(BRAND_NAME);
	}

	public static Brand savedBrand(Long id) {
		return persisted(brand(), id);
	}

	public static Menu menu(Long brandId) {
		return Menu.create(brandId, MENU_NAME);
	}

	public static Menu savedMenu(Long id, Long brandId) {
		return persisted(menu(brandId), id);
	}

	public static OptionPreset optionPreset(Long brandId) {
		return OptionPreset.create(brandId, OPTION_PRESET_NAME);
	}

	public static OptionPreset savedOptionPreset(Long id, Long brandId) {
		return persisted(optionPreset(brandId), id);
	}

	public static CreateBrandRequest createBrandRequest() {
		return new CreateBrandRequest(BRAND_NAME);
	}

	public static CreateMenuRequest createMenuRequest(Long brandId) {
		return new CreateMenuRequest(brandId, MENU_NAME);
	}

	public static CreateOptionPresetRequest createOptionPresetRequest(Long brandId) {
		return new CreateOptionPresetRequest(brandId, OPTION_PRESET_NAME);
	}

	public static BrandResponse brandResponse() {
		return new BrandResponse(BRAND_ID, BRAND_NAME, CREATED_AT);
	}

	public static MenuResponse menuResponse() {
		return new MenuResponse(MENU_ID, BRAND_ID, MENU_NAME, CREATED_AT);
	}

	public static OptionPresetResponse optionPresetResponse() {
		return new OptionPresetResponse(OPTION_PRESET_ID, BRAND_ID, OPTION_PRESET_NAME, CREATED_AT);
	}

	public static String brandJson(String name) {
		return "{\"name\": " + quote(name) + "}";
	}

	public static String brandScopedJson(Long brandId, String name) {
		return "{\"brandId\": " + brandId + ", \"name\": " + quote(name) + "}";
	}

	private static String quote(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}

	public static <T> T persisted(T entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id);
		ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
		return entity;
	}
}
