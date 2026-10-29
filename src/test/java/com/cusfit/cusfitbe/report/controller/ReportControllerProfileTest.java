package com.cusfit.cusfitbe.report.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = ReportController.class)
@ActiveProfiles("test")
class ReportControllerProfileTest {

	@Autowired
	private ApplicationContext context;

	@Test
	@DisplayName("local이 아닌 프로필이면 신고 개발 Controller가 빈으로 등록되지 않는다")
	void devControllerNotRegistered() {
		// when
		String[] beanNames = context.getBeanNamesForType(ReportController.class);

		// then
		assertThat(beanNames).isEmpty();
	}
}
