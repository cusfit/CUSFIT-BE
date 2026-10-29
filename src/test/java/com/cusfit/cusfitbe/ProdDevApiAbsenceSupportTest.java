package com.cusfit.cusfitbe;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("prod")
@Import(PostgisContainerConfig.class)
class ProdDevApiAbsenceSupportTest extends AbstractDevApiAbsenceSupportTest {

	@Override
	protected String expectedProfile() {
		return "prod";
	}
}
