package com.cusfit.cusfitbe.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgisContainerConfig.class)
class CatalogRepositoryTest {

	private static final Long MISSING_BRAND_ID = 999_999L;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private OptionPresetRepository optionPresetRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("브랜드를 저장하고 다시 조회하면 ID, 이름, 생성 시각이 유지된다")
	void saveAndFindBrand() {
		// given
		Brand saved = brandRepository.saveAndFlush(CatalogFixture.brand());
		entityManager.clear();

		// when
		Brand found = brandRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getId()).isNotNull();
		assertThat(found.getName()).isEqualTo(CatalogFixture.BRAND_NAME);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하는 브랜드로 메뉴를 저장하고 다시 조회하면 브랜드 ID와 이름이 유지된다")
	void saveAndFindMenu() {
		// given
		Brand brand = brandRepository.saveAndFlush(CatalogFixture.brand());
		Menu saved = menuRepository.saveAndFlush(CatalogFixture.menu(brand.getId()));
		entityManager.clear();

		// when
		Menu found = menuRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getBrandId()).isEqualTo(brand.getId());
		assertThat(found.getName()).isEqualTo(CatalogFixture.MENU_NAME);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 브랜드 ID로 메뉴를 저장하면 FK 제약 위반이 발생한다")
	void menuBrandForeignKey() {
		// given
		Menu menu = CatalogFixture.menu(MISSING_BRAND_ID);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> menuRepository.saveAndFlush(menu));
	}

	@Test
	@DisplayName("존재하는 브랜드로 옵션 프리셋을 저장하고 다시 조회하면 브랜드 ID와 이름이 유지된다")
	void saveAndFindOptionPreset() {
		// given
		Brand brand = brandRepository.saveAndFlush(CatalogFixture.brand());
		OptionPreset saved = optionPresetRepository.saveAndFlush(CatalogFixture.optionPreset(brand.getId()));
		entityManager.clear();

		// when
		OptionPreset found = optionPresetRepository.findById(saved.getId()).orElseThrow();

		// then
		assertThat(found.getBrandId()).isEqualTo(brand.getId());
		assertThat(found.getName()).isEqualTo(CatalogFixture.OPTION_PRESET_NAME);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 브랜드 ID로 옵션 프리셋을 저장하면 FK 제약 위반이 발생한다")
	void optionPresetBrandForeignKey() {
		// given
		OptionPreset optionPreset = CatalogFixture.optionPreset(MISSING_BRAND_ID);

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> optionPresetRepository.saveAndFlush(optionPreset));
	}

	@Test
	@DisplayName("메뉴를 검색하면 메뉴명 또는 브랜드명이 대소문자 구분 없이 부분 일치하는 메뉴를 브랜드명과 함께 반환한다")
	void searchMenusByMenuOrBrandName() {
		// given
		Brand starbucks = brandRepository.saveAndFlush(Brand.create("Starbucks"));
		Brand gongcha = brandRepository.saveAndFlush(Brand.create("공차"));
		Menu javaChip = menuRepository.saveAndFlush(Menu.create(starbucks.getId(), "자바칩 프라푸치노"));
		Menu latte = menuRepository.saveAndFlush(Menu.create(starbucks.getId(), "카페 라떼"));
		menuRepository.saveAndFlush(Menu.create(gongcha.getId(), "블랙 밀크티"));
		entityManager.clear();

		// when
		List<MenuSearchProjection> byMenuName = menuRepository.search(null, "자바칩");
		List<MenuSearchProjection> byBrandName = menuRepository.search(null, "starBUCKS");

		// then
		assertThat(byMenuName).extracting(MenuSearchProjection::getId).containsExactly(javaChip.getId());
		assertThat(byMenuName.get(0).getBrandName()).isEqualTo("Starbucks");
		assertThat(byBrandName).extracting(MenuSearchProjection::getId).containsExactly(javaChip.getId(), latte.getId());
	}

	@Test
	@DisplayName("브랜드 ID와 빈 keyword로 메뉴를 검색하면 그 브랜드의 메뉴만 모두 반환한다")
	void searchMenusByBrandId() {
		// given
		Brand starbucks = brandRepository.saveAndFlush(Brand.create("스타벅스"));
		Brand gongcha = brandRepository.saveAndFlush(Brand.create("공차"));
		menuRepository.saveAndFlush(Menu.create(starbucks.getId(), "카페 라떼"));
		Menu milkTea = menuRepository.saveAndFlush(Menu.create(gongcha.getId(), "블랙 밀크티"));
		entityManager.clear();

		// when
		List<MenuSearchProjection> result = menuRepository.search(gongcha.getId(), "");

		// then
		assertThat(result).extracting(MenuSearchProjection::getId).containsExactly(milkTea.getId());
	}

	@Test
	@DisplayName("브랜드 ID로 옵션 프리셋을 조회하면 그 브랜드의 프리셋만 ID 순으로 반환한다")
	void findOptionPresetsByBrandId() {
		// given
		Brand brand = brandRepository.saveAndFlush(CatalogFixture.brand());
		Brand other = brandRepository.saveAndFlush(Brand.create("공차"));
		OptionPreset first = optionPresetRepository.saveAndFlush(OptionPreset.create(brand.getId(), "샷 추가"));
		OptionPreset second = optionPresetRepository.saveAndFlush(OptionPreset.create(brand.getId(), "휘핑 추가"));
		optionPresetRepository.saveAndFlush(OptionPreset.create(other.getId(), "펄 추가"));
		entityManager.clear();

		// when
		List<OptionPreset> result = optionPresetRepository.findByBrandIdOrderByIdAsc(brand.getId());

		// then
		assertThat(result).extracting(OptionPreset::getId).containsExactly(first.getId(), second.getId());
	}
}
