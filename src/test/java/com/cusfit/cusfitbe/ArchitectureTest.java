package com.cusfit.cusfitbe;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

	private static final String BASE_PACKAGE = "com.cusfit.cusfitbe";

	private static JavaClasses importedClasses() {
		return new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages(BASE_PACKAGE);
	}

	@Test
	void 컨트롤러는_리포지토리를_직접_참조하지_않는다() {
		// 아직 controller 패키지가 없는 스켈레톤 단계라 allowEmptyShould로 공집합도 허용한다.
		// 첫 Controller가 추가되면 이 옵션 없이도 규칙이 유효하게 검증된다.
		ArchRule rule = noClasses()
				.that().resideInAPackage("..controller..")
				.should().dependOnClassesThat().resideInAPackage("..repository..")
				.allowEmptyShould(true);

		rule.check(importedClasses());
	}

	@Test
	void 엔티티는_상위_계층에_의존하지_않는다() {
		ArchRule rule = noClasses()
				.that().resideInAPackage("..entity..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"..controller..", "..service..", "..dto..", "..repository..")
				.allowEmptyShould(true);

		rule.check(importedClasses());
	}

	@Test
	void 다른_도메인의_entity_repository_service_impl을_직접_참조하지_않는다() {
		// 도메인 간 허용 경로는 상대 도메인의 service 인터페이스와 event뿐이다 (CODE_CONVENTIONS 7.6).
		ArchRule rule = classes()
				.that().resideOutsideOfPackage("..global..")
				.should(notDependOnInternalsOfOtherDomains())
				.allowEmptyShould(true);

		rule.check(importedClasses());
	}

	@Test
	void 엔티티는_public_세터를_노출하지_않는다() {
		ArchRule rule = noMethods()
				.that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
				.and().arePublic()
				.and().haveNameMatching("set[A-Z].*")
				.should().bePublic()
				.allowEmptyShould(true);

		rule.check(importedClasses());
	}

	@Test
	void 엔티티의_기본_생성자는_protected이다() {
		ArchRule rule = classes()
				.that().areAnnotatedWith(Entity.class)
				.should(haveProtectedNoArgConstructor())
				.allowEmptyShould(true);

		rule.check(importedClasses());
	}

	private static ArchCondition<JavaClass> haveProtectedNoArgConstructor() {
		return new ArchCondition<>("protected 기본 생성자를 가진다") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				boolean satisfied = javaClass.getConstructors().stream()
						.anyMatch(c -> c.getRawParameterTypes().isEmpty()
								&& c.getModifiers().contains(JavaModifier.PROTECTED));
				if (!satisfied) {
					events.add(SimpleConditionEvent.violated(javaClass,
							javaClass.getName() + "에 protected 기본 생성자가 없습니다"));
				}
			}
		};
	}

	private static ArchCondition<JavaClass> notDependOnInternalsOfOtherDomains() {
		return new ArchCondition<>("다른 도메인의 entity/repository/service.impl에 의존하지 않는다") {
			@Override
			public void check(JavaClass origin, ConditionEvents events) {
				String originDomain = domainOf(origin);
				for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
					JavaClass target = dependency.getTargetClass();
					String targetDomain = domainOf(target);
					if (originDomain == null || targetDomain == null || originDomain.equals(targetDomain)) {
						continue;
					}
					String targetPackage = target.getPackageName();
					boolean internal = targetPackage.contains(".entity")
							|| targetPackage.contains(".repository")
							|| targetPackage.contains(".service.impl");
					if (internal) {
						events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
					}
				}
			}
		};
	}

	/** com.cusfit.cusfitbe.{domain}... 에서 domain을 반환한다. 기본 패키지 바깥이거나 global이면 null. */
	private static String domainOf(JavaClass javaClass) {
		String packageName = javaClass.getPackageName();
		String prefix = BASE_PACKAGE + ".";
		if (!packageName.startsWith(prefix)) {
			return null;
		}
		String domain = packageName.substring(prefix.length()).split("\\.")[0];
		return domain.equals("global") ? null : domain;
	}
}
