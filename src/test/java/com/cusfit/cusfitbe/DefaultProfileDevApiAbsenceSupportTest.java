package com.cusfit.cusfitbe;

import com.cusfit.cusfitbe.support.PostgisContainerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Gradle test 태스크의 spring.profiles.active=test를 덮어쓰기 위해 default 프로필을 명시적으로 활성화한다. */
@ActiveProfiles("default")
@Import(PostgisContainerConfig.class)
class DefaultProfileDevApiAbsenceSupportTest extends AbstractDevApiAbsenceSupportTest {

	@Override
	protected String expectedProfile() {
		return "default";
	}
}
