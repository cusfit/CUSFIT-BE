package com.cusfit.cusfitbe.terms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTermsRequest(
		@NotBlank String type,
		@NotBlank String version,
		@NotBlank String contentUrl,
		@NotNull Instant effectiveAt
) {
}
