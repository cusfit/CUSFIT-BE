package com.cusfit.cusfitbe.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseCreatedEntityFoundationTest {

	@Test
	@DisplayName("영속화 전 기반 엔티티를 생성하면 id와 createdAt이 비어 있다")
	void beforePersist_idAndCreatedAtAreNull() {
		// given
		TestCreatedEntity entity = new TestCreatedEntity();

		// when
		Long id = entity.getId();
		Instant createdAt = entity.getCreatedAt();

		// then
		assertThat(id).isNull();
		assertThat(createdAt).isNull();
	}

	@Test
	@DisplayName("PrePersist 콜백을 실행하면 createdAt이 현재 시각으로 설정된다")
	void prePersist_setsCreatedAt() throws Exception {
		// given
		TestCreatedEntity entity = new TestCreatedEntity();
		Method prePersist = prePersistMethod();
		Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

		// when
		prePersist.invoke(entity);

		// then
		Instant after = Instant.now();
		assertThat(entity.getCreatedAt()).isNotNull();
		assertThat(entity.getCreatedAt()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
	}

	@Test
	@DisplayName("기반 엔티티 클래스를 확인하면 MappedSuperclass이고 PrePersist 콜백이 하나 있다")
	void classLevelMapping() {
		// given
		Class<BaseCreatedEntity> type = BaseCreatedEntity.class;

		// when
		boolean mappedSuperclass = type.isAnnotationPresent(MappedSuperclass.class);
		List<Method> callbacks = prePersistMethods();

		// then
		assertThat(mappedSuperclass).isTrue();
		assertThat(callbacks).hasSize(1);
	}

	@Test
	@DisplayName("id 필드를 확인하면 Long 타입이며 IDENTITY 전략으로 생성된다")
	void idFieldMapping() throws Exception {
		// given
		Field id = BaseCreatedEntity.class.getDeclaredField("id");

		// when
		GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);

		// then
		assertThat(id.getType()).isEqualTo(Long.class);
		assertThat(id.isAnnotationPresent(Id.class)).isTrue();
		assertThat(generatedValue).isNotNull();
		assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);
	}

	@Test
	@DisplayName("createdAt 필드를 확인하면 NOT NULL Instant 컬럼이다")
	void createdAtFieldMapping() throws Exception {
		// given
		Field createdAt = BaseCreatedEntity.class.getDeclaredField("createdAt");

		// when
		Column column = createdAt.getAnnotation(Column.class);

		// then
		assertThat(createdAt.getType()).isEqualTo(Instant.class);
		assertThat(column).isNotNull();
		assertThat(column.nullable()).isFalse();
	}

	@Test
	@DisplayName("기반 엔티티를 확인하면 updatedAt 필드와 public 세터가 없다")
	void noUpdatedAtAndNoSetter() {
		// given
		Class<BaseCreatedEntity> type = BaseCreatedEntity.class;

		// when
		List<String> fieldNames = Arrays.stream(type.getDeclaredFields()).map(Field::getName).toList();
		List<String> publicSetters = Arrays.stream(type.getMethods())
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				.map(Method::getName)
				.filter(name -> name.matches("set[A-Z].*"))
				.toList();

		// then
		assertThat(fieldNames).doesNotContain("updatedAt");
		assertThat(publicSetters).isEmpty();
	}

	private static Method prePersistMethod() {
		Method method = prePersistMethods().get(0);
		method.setAccessible(true);
		return method;
	}

	private static List<Method> prePersistMethods() {
		return Arrays.stream(BaseCreatedEntity.class.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(PrePersist.class))
				.toList();
	}

	static class TestCreatedEntity extends BaseCreatedEntity {
	}
}
