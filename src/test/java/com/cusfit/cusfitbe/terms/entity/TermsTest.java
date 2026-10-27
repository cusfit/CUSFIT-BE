package com.cusfit.cusfitbe.terms.entity;

import static com.cusfit.cusfitbe.terms.fixture.TermsFixture.CONTENT_URL;
import static com.cusfit.cusfitbe.terms.fixture.TermsFixture.EFFECTIVE_AT;
import static com.cusfit.cusfitbe.terms.fixture.TermsFixture.TYPE;
import static com.cusfit.cusfitbe.terms.fixture.TermsFixture.VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TermsTest {

	@Test
	@DisplayName("유효한 값으로 약관을 생성하면 모든 필드가 설정되고 id와 createdAt은 비어 있다")
	void create() {
		// given
		// when
		Terms terms = Terms.create(TYPE, VERSION, CONTENT_URL, EFFECTIVE_AT);

		// then
		assertThat(terms).isInstanceOf(BaseCreatedEntity.class);
		assertThat(terms.getType()).isEqualTo(TYPE);
		assertThat(terms.getVersion()).isEqualTo(VERSION);
		assertThat(terms.getContentUrl()).isEqualTo(CONTENT_URL);
		assertThat(terms.getEffectiveAt()).isEqualTo(EFFECTIVE_AT);
		assertThat(terms.getId()).isNull();
		assertThat(terms.getCreatedAt()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("type이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankType(String type) {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Terms.create(type, VERSION, CONTENT_URL, EFFECTIVE_AT));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("version이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankVersion(String version) {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Terms.create(TYPE, version, CONTENT_URL, EFFECTIVE_AT));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("contentUrl이 비어 있으면 INVALID_REQUEST 예외가 발생한다")
	void blankContentUrl(String contentUrl) {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Terms.create(TYPE, VERSION, contentUrl, EFFECTIVE_AT));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	@DisplayName("effectiveAt이 없으면 INVALID_REQUEST 예외가 발생한다")
	void nullEffectiveAt() {
		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> Terms.create(TYPE, VERSION, CONTENT_URL, null));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
