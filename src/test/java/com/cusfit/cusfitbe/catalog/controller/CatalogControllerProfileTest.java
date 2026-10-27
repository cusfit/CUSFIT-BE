package com.cusfit.cusfitbe.catalog.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = {BrandController.class, MenuController.class, OptionPresetController.class})
@ActiveProfiles("test")
class CatalogControllerProfileTest {

	@Autowired
	private ApplicationContext context;

	@Test
	@DisplayName("local이 아닌 프로필이면 카탈로그 개발 Controller가 빈으로 등록되지 않는다")
	void devControllersNotRegistered() {
		// given
		Class<?>[] controllers = {BrandController.class, MenuController.class, OptionPresetController.class};

		// when & then
		for (Class<?> controller : controllers) {
			assertThat(context.getBeanNamesForType(controller)).isEmpty();
		}
	}
}
