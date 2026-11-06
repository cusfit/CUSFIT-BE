package com.cusfit.cusfitbe.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
	CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"),
	// 골격만 있는 운영 API. 스키마나 인증 결정(docs/API_DOCS.md D1 등)이 끝나면 구현한다.
	NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");

	private final HttpStatus status;
	private final String defaultMessage;
}
