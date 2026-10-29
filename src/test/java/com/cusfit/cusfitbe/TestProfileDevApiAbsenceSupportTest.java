package com.cusfit.cusfitbe;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(PostgisContainerConfig.class)
class TestProfileDevApiAbsenceSupportTest extends AbstractDevApiAbsenceSupportTest {

	@Override
	protected String expectedProfile() {
		return "test";
	}
}
