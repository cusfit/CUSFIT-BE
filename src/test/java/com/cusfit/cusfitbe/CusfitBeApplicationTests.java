package com.cusfit.cusfitbe;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgisContainerConfig.class)
class CusfitBeApplicationTests {

	@Test
	void contextLoads() {
	}

}
