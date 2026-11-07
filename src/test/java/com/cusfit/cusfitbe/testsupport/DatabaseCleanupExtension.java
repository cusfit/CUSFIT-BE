package com.cusfit.cusfitbe.testsupport;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** 각 시나리오 테스트가 끝날 때마다 {@link DatabaseCleaner}로 데이터를 비운다. */
public class DatabaseCleanupExtension implements AfterEachCallback {

	@Override
	public void afterEach(ExtensionContext context) {
		SpringExtension.getApplicationContext(context).getBean(DatabaseCleaner.class).clean();
	}
}
