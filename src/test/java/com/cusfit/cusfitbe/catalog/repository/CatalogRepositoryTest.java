package com.cusfit.cusfitbe.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import com.cusfit.cusfitbe.catalog.fixture.CatalogFixture;
import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
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
}
