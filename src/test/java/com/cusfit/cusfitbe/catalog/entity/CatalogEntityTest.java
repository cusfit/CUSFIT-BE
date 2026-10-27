package com.cusfit.cusfitbe.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CatalogEntityTest {

	@Nested
	@DisplayName("Brand")
	class BrandCreate {

		@Test
		@DisplayName("유효한 이름으로 생성하면 이름을 가진 브랜드가 만들어진다")
		void create() {
			// given
			String name = CatalogFixture.BRAND_NAME;

			// when
			Brand brand = Brand.create(name);

			// then
			assertThat(brand.getName()).isEqualTo(name);
			assertThat(brand.getId()).isNull();
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" ", "\t"})
		@DisplayName("이름이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
		void blankName(String name) {
			// given
			String invalidName = name;

			// when
			BusinessException exception = assertThrows(BusinessException.class, () -> Brand.create(invalidName));

			// then
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		}
	}

	@Nested
	@DisplayName("Menu")
	class MenuCreate {

		@Test
		@DisplayName("브랜드 ID와 유효한 이름으로 생성하면 메뉴가 만들어진다")
		void create() {
			// given
			Long brandId = CatalogFixture.BRAND_ID;
			String name = CatalogFixture.MENU_NAME;

			// when
			Menu menu = Menu.create(brandId, name);

			// then
			assertThat(menu.getBrandId()).isEqualTo(brandId);
			assertThat(menu.getName()).isEqualTo(name);
		}

		@Test
		@DisplayName("브랜드 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
		void nullBrandId() {
			// given
			String name = CatalogFixture.MENU_NAME;

			// when
			BusinessException exception = assertThrows(BusinessException.class, () -> Menu.create(null, name));

			// then
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" "})
		@DisplayName("이름이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
		void blankName(String name) {
			// given
			Long brandId = CatalogFixture.BRAND_ID;

			// when
			BusinessException exception = assertThrows(BusinessException.class, () -> Menu.create(brandId, name));

			// then
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		}
	}

	@Nested
	@DisplayName("OptionPreset")
	class OptionPresetCreate {

		@Test
		@DisplayName("브랜드 ID와 유효한 이름으로 생성하면 옵션 프리셋이 만들어진다")
		void create() {
			// given
			Long brandId = CatalogFixture.BRAND_ID;
			String name = CatalogFixture.OPTION_PRESET_NAME;

			// when
			OptionPreset optionPreset = OptionPreset.create(brandId, name);

			// then
			assertThat(optionPreset.getBrandId()).isEqualTo(brandId);
			assertThat(optionPreset.getName()).isEqualTo(name);
		}

		@Test
		@DisplayName("브랜드 ID가 없으면 INVALID_REQUEST 예외가 발생한다")
		void nullBrandId() {
			// given
			String name = CatalogFixture.OPTION_PRESET_NAME;

			// when
			BusinessException exception = assertThrows(BusinessException.class,
					() -> OptionPreset.create(null, name));

			// then
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" "})
		@DisplayName("이름이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
		void blankName(String name) {
			// given
			Long brandId = CatalogFixture.BRAND_ID;

			// when
			BusinessException exception = assertThrows(BusinessException.class,
					() -> OptionPreset.create(brandId, name));

			// then
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		}
	}
}
