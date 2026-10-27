package com.cusfit.cusfitbe.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.impl.BrandWriteServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.MenuWriteServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.OptionPresetWriteServiceImpl;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogWriteServiceTest {

	@Mock
	private BrandRepository brandRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private OptionPresetRepository optionPresetRepository;

	@Mock
	private BrandReadService brandReadService;

	@InjectMocks
	private BrandWriteServiceImpl brandWriteService;

	@InjectMocks
	private MenuWriteServiceImpl menuWriteService;

	@InjectMocks
	private OptionPresetWriteServiceImpl optionPresetWriteService;

	@Test
	@DisplayName("브랜드 생성 요청을 저장하면 저장된 브랜드 응답을 반환한다")
	void createBrand() {
		// given
		given(brandRepository.save(any(Brand.class)))
				.willAnswer(invocation -> CatalogFixture.persisted(invocation.getArgument(0), CatalogFixture.BRAND_ID));

		// when
		BrandResponse response = brandWriteService.create(CatalogFixture.createBrandRequest());

		// then
		assertThat(response).isEqualTo(CatalogFixture.brandResponse());
	}

	@Test
	@DisplayName("존재하는 브랜드로 메뉴를 생성하면 저장된 메뉴 응답을 반환한다")
	void createMenu() {
		// given
		CreateMenuRequest request = CatalogFixture.createMenuRequest(CatalogFixture.BRAND_ID);
		given(brandReadService.getById(CatalogFixture.BRAND_ID)).willReturn(CatalogFixture.brandResponse());
		given(menuRepository.save(any(Menu.class)))
				.willAnswer(invocation -> CatalogFixture.persisted(invocation.getArgument(0), CatalogFixture.MENU_ID));

		// when
		MenuResponse response = menuWriteService.create(request);

		// then
		assertThat(response).isEqualTo(CatalogFixture.menuResponse());
	}

	@Test
	@DisplayName("없는 브랜드로 메뉴를 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createMenuWithMissingBrand() {
		// given
		CreateMenuRequest request = CatalogFixture.createMenuRequest(CatalogFixture.BRAND_ID);
		given(brandReadService.getById(CatalogFixture.BRAND_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> menuWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		verify(menuRepository, never()).save(any());
	}

	@Test
	@DisplayName("존재하는 브랜드로 옵션 프리셋을 생성하면 저장된 옵션 프리셋 응답을 반환한다")
	void createOptionPreset() {
		// given
		CreateOptionPresetRequest request = CatalogFixture.createOptionPresetRequest(CatalogFixture.BRAND_ID);
		given(brandReadService.getById(CatalogFixture.BRAND_ID)).willReturn(CatalogFixture.brandResponse());
		given(optionPresetRepository.save(any(OptionPreset.class)))
				.willAnswer(invocation -> CatalogFixture.persisted(invocation.getArgument(0),
						CatalogFixture.OPTION_PRESET_ID));

		// when
		OptionPresetResponse response = optionPresetWriteService.create(request);

		// then
		assertThat(response).isEqualTo(CatalogFixture.optionPresetResponse());
	}

	@Test
	@DisplayName("없는 브랜드로 옵션 프리셋을 생성하면 NOT_FOUND 예외가 발생하고 저장하지 않는다")
	void createOptionPresetWithMissingBrand() {
		// given
		CreateOptionPresetRequest request = CatalogFixture.createOptionPresetRequest(CatalogFixture.BRAND_ID);
		given(brandReadService.getById(CatalogFixture.BRAND_ID))
				.willThrow(new BusinessException(ErrorCode.NOT_FOUND));

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> optionPresetWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		verify(optionPresetRepository, never()).save(any());
	}
}
