package com.cusfit.cusfitbe.recipe.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = RecipeController.class)
@ActiveProfiles("test")
class RecipeControllerProfileTest {

	@Autowired
	private ApplicationContext context;

	@Test
	@DisplayName("local이 아닌 프로필이면 레시피 개발 Controller가 빈으로 등록되지 않는다")
	void devControllerNotRegistered() {
		// when
		String[] beanNames = context.getBeanNamesForType(RecipeController.class);

		// then
		assertThat(beanNames).isEmpty();
	}
}
