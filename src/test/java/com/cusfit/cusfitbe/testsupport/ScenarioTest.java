package com.cusfit.cusfitbe.testsupport;

import com.cusfit.cusfitbe.global.config.PostgisContainerConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * 여러 도메인이 함께 움직이는 시나리오 테스트용 합성 애너테이션.
 * 실제 빈과 PostgreSQL(Testcontainers)을 쓰고, {@code @Transactional}을 붙이지 않아 커밋 후 동작까지 검증한다.
 * 테스트가 끝나면 모든 테이블을 비운다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest
@Import({PostgisContainerConfig.class, ScenarioTestConfig.class})
@ExtendWith(DatabaseCleanupExtension.class)
public @interface ScenarioTest {
}
