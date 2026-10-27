package com.cusfit.cusfitbe.global.validation;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;

public final class DomainValidation {

	private DomainValidation() {
	}

	public static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, field + " must not be blank");
		}
	}

	public static void requirePresent(Object value, String field) {
		if (value == null) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, field + " is required");
		}
	}
}
