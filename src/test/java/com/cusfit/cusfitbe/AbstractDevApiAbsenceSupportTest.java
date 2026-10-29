package com.cusfit.cusfitbe;

import static org.assertj.core.api.Assertions.assertThat;

import com.cusfit.cusfitbe.catalog.controller.BrandController;
import com.cusfit.cusfitbe.catalog.controller.MenuController;
import com.cusfit.cusfitbe.catalog.controller.OptionPresetController;
import com.cusfit.cusfitbe.notification.controller.NotificationController;
import com.cusfit.cusfitbe.recipe.controller.RecipeController;
import com.cusfit.cusfitbe.report.controller.ReportController;
import com.cusfit.cusfitbe.support.controller.FaqController;
import com.cusfit.cusfitbe.support.controller.InquiryController;
import com.cusfit.cusfitbe.support.controller.NoticeController;
import com.cusfit.cusfitbe.terms.controller.TermsController;
import com.cusfit.cusfitbe.user.controller.UserController;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * local이 아닌 프로필의 실제 애플리케이션 컨텍스트에 도메인 개발 Controller와 /api/dev 매핑이 없는지 확인한다.
 * 하위 클래스가 {@code @ActiveProfiles}와 {@code @Import(PostgisContainerConfig.class)}를 지정한다.
 * SecurityConfig 테스트의 중첩 스텁 Controller는 도메인 경로로 세지 않는다.
 */
@SpringBootTest
abstract class AbstractDevApiAbsenceSupportTest {

	private static final String BASE_PACKAGE = "com.cusfit.cusfitbe.";
	private static final List<Class<?>> DEV_CONTROLLERS = List.of(UserController.class, TermsController.class,
			BrandController.class, MenuController.class, OptionPresetController.class, RecipeController.class,
			ReportController.class, NotificationController.class, NoticeController.class, FaqController.class,
			InquiryController.class);

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Environment environment;

	protected abstract String expectedProfile();

	@Test
	@DisplayName("local이 아닌 프로필이면 도메인 개발 Controller가 빈으로 등록되지 않는다")
	void devControllersNotRegistered() {
		// given
		String[] activeProfiles = environment.getActiveProfiles();

		// when
		List<String> domainRestControllers = context.getBeansWithAnnotation(RestController.class).values().stream()
				.map(AopUtils::getTargetClass)
				.filter(AbstractDevApiAbsenceSupportTest::isDomainClass)
				.map(Class::getName)
				.toList();

		// then
		assertThat(activeProfiles).containsExactly(expectedProfile());
		for (Class<?> controller : DEV_CONTROLLERS) {
			assertThat(context.getBeanNamesForType(controller)).as(controller.getSimpleName()).isEmpty();
		}
		assertThat(domainRestControllers).isEmpty();
	}

	@Test
	@DisplayName("local이 아닌 프로필이면 /api/dev 경로의 요청 매핑이 등록되지 않는다")
	void devMappingsNotRegistered() {
		// given
		List<RequestMappingHandlerMapping> handlerMappings =
				List.copyOf(context.getBeansOfType(RequestMappingHandlerMapping.class).values());

		// when
		List<String> devPatterns = handlerMappings.stream()
				.flatMap(mapping -> mapping.getHandlerMethods().entrySet().stream())
				.filter(entry -> !isTestStub(entry.getValue()))
				.map(Map.Entry::getKey)
				.map(RequestMappingInfo::getPatternValues)
				.flatMap(Set::stream)
				.filter(pattern -> pattern.startsWith("/api/dev"))
				.toList();

		// then
		assertThat(devPatterns).isEmpty();
	}

	private static boolean isDomainClass(Class<?> type) {
		String packageName = type.getPackageName();
		return packageName.startsWith(BASE_PACKAGE) && !packageName.startsWith(BASE_PACKAGE + "global");
	}

	private static boolean isTestStub(HandlerMethod handlerMethod) {
		Class<?> enclosing = handlerMethod.getBeanType().getEnclosingClass();
		return enclosing != null && enclosing.getSimpleName().endsWith("Test");
	}
}
