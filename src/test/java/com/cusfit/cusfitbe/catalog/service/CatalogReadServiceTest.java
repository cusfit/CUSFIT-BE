package com.cusfit.cusfitbe.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.catalog.repository.MenuSearchProjection;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.impl.BrandReadServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.MenuReadServiceImpl;
import com.cusfit.cusfitbe.catalog.service.impl.OptionPresetReadServiceImpl;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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

	@Test
	@DisplayName("브랜드 목록을 조회하면 ID 순으로 전체 브랜드를 반환한다")
	void getAllBrands() {
		// given
		given(brandRepository.findAll(Sort.by("id")))
				.willReturn(List.of(CatalogFixture.savedBrand(CatalogFixture.BRAND_ID)));

		// when
		List<BrandResponse> responses = brandReadService.getAll();

		// then
		assertThat(responses).containsExactly(CatalogFixture.brandResponse());
	}

	@Test
	@DisplayName("keyword 없이 메뉴를 검색하면 빈 문자열로 검색하고 브랜드명을 포함해 반환한다")
	void searchMenusWithoutKeyword() {
		// given
		MenuSearchProjection projection = menuProjection();
		given(menuRepository.search(CatalogFixture.BRAND_ID, "")).willReturn(List.of(projection));

		// when
		List<MenuSummaryResponse> responses = menuReadService.search(CatalogFixture.BRAND_ID, null);

		// then
		assertThat(responses).containsExactly(new MenuSummaryResponse(CatalogFixture.MENU_ID, CatalogFixture.BRAND_ID,
				CatalogFixture.BRAND_NAME, CatalogFixture.MENU_NAME, CatalogFixture.CREATED_AT));
	}

	@Test
	@DisplayName("메뉴 검색 keyword의 앞뒤 공백은 제거하고 검색한다")
	void searchMenusStripsKeyword() {
		// given
		given(menuRepository.search(null, "아메리카노")).willReturn(List.of());

		// when
		List<MenuSummaryResponse> responses = menuReadService.search(null, "  아메리카노 ");

		// then
		assertThat(responses).isEmpty();
	}

	@Test
	@DisplayName("존재하는 브랜드의 옵션 프리셋을 조회하면 프리셋 목록을 반환한다")
	void getOptionPresetsByBrand() {
		// given
		given(brandRepository.existsById(CatalogFixture.BRAND_ID)).willReturn(true);
		given(optionPresetRepository.findByBrandIdOrderByIdAsc(CatalogFixture.BRAND_ID)).willReturn(
				List.of(CatalogFixture.savedOptionPreset(CatalogFixture.OPTION_PRESET_ID, CatalogFixture.BRAND_ID)));

		// when
		List<OptionPresetResponse> responses = optionPresetReadService.getByBrandId(CatalogFixture.BRAND_ID);

		// then
		assertThat(responses).containsExactly(CatalogFixture.optionPresetResponse());
	}

	@Test
	@DisplayName("존재하지 않는 브랜드의 옵션 프리셋을 조회하면 NOT_FOUND 예외가 발생한다")
	void getOptionPresetsByMissingBrand() {
		// given
		given(brandRepository.existsById(CatalogFixture.BRAND_ID)).willReturn(false);

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> optionPresetReadService.getByBrandId(CatalogFixture.BRAND_ID));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	private static MenuSearchProjection menuProjection() {
		return new MenuSearchProjection() {
			public Long getId() {
				return CatalogFixture.MENU_ID;
			}

			public Long getBrandId() {
				return CatalogFixture.BRAND_ID;
			}

			public String getBrandName() {
				return CatalogFixture.BRAND_NAME;
			}

			public String getName() {
				return CatalogFixture.MENU_NAME;
			}

			public Instant getCreatedAt() {
				return CatalogFixture.CREATED_AT;
			}
		};
	}
}
