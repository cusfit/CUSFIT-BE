package com.cusfit.cusfitbe.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** DB가 필요한 테스트가 {@code @Import}로 공유하는 PostGIS Testcontainers 설정. */
@TestConfiguration(proxyBeanMethods = false)
public class PostgisContainerConfig {

	private static final DockerImageName POSTGIS_IMAGE =
			DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgisContainer() {
		return new PostgreSQLContainer<>(POSTGIS_IMAGE);
	}
}
