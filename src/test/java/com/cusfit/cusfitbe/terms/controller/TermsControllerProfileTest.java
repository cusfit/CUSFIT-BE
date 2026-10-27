package com.cusfit.cusfitbe.terms.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = TermsController.class)
@ActiveProfiles("test")
class TermsControllerProfileTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("local이 아닌 프로필에서는 약관 개발 Controller가 빈으로 등록되지 않는다")
	void notRegisteredOutsideLocal() {
		// given
		Class<TermsController> controllerType = TermsController.class;

		// when
		String[] beanNames = applicationContext.getBeanNamesForType(controllerType);

		// then
		assertThat(beanNames).isEmpty();
	}
}
