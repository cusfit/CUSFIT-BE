package com.cusfit.cusfitbe.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = NotificationController.class)
@ActiveProfiles("test")
class NotificationControllerProfileTest {

	@Autowired
	private ApplicationContext context;

	@Test
	@DisplayName("local이 아닌 프로필이면 알림 개발 Controller가 빈으로 등록되지 않는다")
	void devControllerNotRegistered() {
		// given
		Class<?> controller = NotificationController.class;

		// when
		String[] beanNames = context.getBeanNamesForType(controller);

		// then
		assertThat(beanNames).isEmpty();
	}
}
