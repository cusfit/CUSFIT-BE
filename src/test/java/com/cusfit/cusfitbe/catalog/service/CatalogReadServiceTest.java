package com.cusfit.cusfitbe.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.impl.BrandReadServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.MenuReadServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.OptionPresetReadServiceImpl;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogReadServiceTest {

	@Mock
	private BrandRepository brandRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private OptionPresetRepository optionPresetRepository;

	@InjectMocks
	private BrandReadServiceImpl brandReadService;

	@InjectMocks
	private MenuReadServiceImpl menuReadService;

	@InjectMocks
	private OptionPresetReadServiceImpl optionPresetReadService;

	@Test
	@DisplayName("존재하는 브랜드 ID로 조회하면 브랜드 응답을 반환한다")
	void getBrand() {
		// given
		given(brandRepository.findById(CatalogFixture.BRAND_ID))
				.willReturn(Optional.of(CatalogFixture.savedBrand(CatalogFixture.BRAND_ID)));

		// when
		BrandResponse response = brandReadService.getById(CatalogFixture.BRAND_ID);

		// then
		assertThat(response).isEqualTo(CatalogFixture.brandResponse());
	}

	@Test
	@DisplayName("없는 브랜드 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getBrandNotFound() {
		// given
		given(brandRepository.findById(CatalogFixture.BRAND_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> brandReadService.getById(CatalogFixture.BRAND_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("존재하는 메뉴 ID로 조회하면 메뉴 응답을 반환한다")
	void getMenu() {
		// given
		given(menuRepository.findById(CatalogFixture.MENU_ID))
				.willReturn(Optional.of(CatalogFixture.savedMenu(CatalogFixture.MENU_ID, CatalogFixture.BRAND_ID)));

		// when
		MenuResponse response = menuReadService.getById(CatalogFixture.MENU_ID);

		// then
		assertThat(response).isEqualTo(CatalogFixture.menuResponse());
	}

	@Test
	@DisplayName("없는 메뉴 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getMenuNotFound() {
		// given
		given(menuRepository.findById(CatalogFixture.MENU_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> menuReadService.getById(CatalogFixture.MENU_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	@DisplayName("존재하는 옵션 프리셋 ID로 조회하면 옵션 프리셋 응답을 반환한다")
	void getOptionPreset() {
		// given
		given(optionPresetRepository.findById(CatalogFixture.OPTION_PRESET_ID))
				.willReturn(Optional.of(
						CatalogFixture.savedOptionPreset(CatalogFixture.OPTION_PRESET_ID, CatalogFixture.BRAND_ID)));

		// when
		OptionPresetResponse response = optionPresetReadService.getById(CatalogFixture.OPTION_PRESET_ID);

		// then
		assertThat(response).isEqualTo(CatalogFixture.optionPresetResponse());
	}

	@Test
	@DisplayName("없는 옵션 프리셋 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getOptionPresetNotFound() {
		// given
		given(optionPresetRepository.findById(CatalogFixture.OPTION_PRESET_ID)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> optionPresetReadService.getById(CatalogFixture.OPTION_PRESET_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
