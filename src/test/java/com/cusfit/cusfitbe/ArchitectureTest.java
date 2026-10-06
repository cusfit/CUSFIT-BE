package com.cusfit.cusfitbe;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

	private static final String BASE_PACKAGE = "com.cusfit.cusfitbe";

	@Test
	void 컨트롤러는_리포지토리를_직접_참조하지_않는다() {
		// 아직 controller 패키지가 없는 스켈레톤 단계라 allowEmptyShould로 공집합도 허용한다.
		// 첫 Controller가 추가되면 이 옵션 없이도 규칙이 유효하게 검증된다.
		ArchRule rule = noClasses()
				.that().resideInAPackage("..controller..")
				.should().dependOnClassesThat().resideInAPackage("..repository..")
				.allowEmptyShould(true);

		rule.check(new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages(BASE_PACKAGE));
	}
}
