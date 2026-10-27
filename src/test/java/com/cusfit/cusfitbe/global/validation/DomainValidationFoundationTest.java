package com.cusfit.cusfitbe.global.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DomainValidationFoundationTest {

	@Test
	@DisplayName("공백이 아닌 문자열을 검사하면 예외 없이 통과한다")
	void requireText_valid() {
		// given
		String value = "커스핏";

		// when & then
		assertDoesNotThrow(() -> DomainValidation.requireText(value, "nickname"));
	}

	@Test
	@DisplayName("앞뒤 공백이 있어도 내용이 있는 문자열을 검사하면 예외 없이 통과한다")
	void requireText_validWithSurroundingSpaces() {
		// given
		String value = "  커스핏  ";

		// when & then
		assertDoesNotThrow(() -> DomainValidation.requireText(value, "nickname"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   ", "\t", "\n", " \t\n "})
	@DisplayName("null이거나 빈 문자열이거나 공백뿐인 문자열을 검사하면 INVALID_REQUEST 예외가 발생한다")
	void requireText_invalid(String value) {
		// given
		String field = "nickname";

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> DomainValidation.requireText(value, field));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("null이 아닌 ID를 검사하면 예외 없이 통과한다")
	void requirePresent_validId() {
		// given
		Long value = 1L;

		// when & then
		assertDoesNotThrow(() -> DomainValidation.requirePresent(value, "brandId"));
	}

	@Test
	@DisplayName("null이 아닌 Instant를 검사하면 예외 없이 통과한다")
	void requirePresent_validInstant() {
		// given
		Instant value = Instant.parse("2026-01-01T00:00:00Z");

		// when & then
		assertDoesNotThrow(() -> DomainValidation.requirePresent(value, "effectiveAt"));
	}

	@Test
	@DisplayName("null 값을 검사하면 INVALID_REQUEST 예외가 발생한다")
	void requirePresent_null() {
		// given
		Object value = null;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> DomainValidation.requirePresent(value, "brandId"));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
