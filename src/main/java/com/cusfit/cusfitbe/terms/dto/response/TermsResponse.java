package com.cusfit.cusfitbe.terms.dto.response;

import com.cusfit.cusfitbe.terms.entity.Terms;
import java.time.Instant;

public record TermsResponse(
		Long id,
		String type,
		String version,
		String contentUrl,
		Instant effectiveAt,
		Instant createdAt
) {
	public static TermsResponse from(Terms terms) {
		return new TermsResponse(terms.getId(), terms.getType(), terms.getVersion(),
				terms.getContentUrl(), terms.getEffectiveAt(), terms.getCreatedAt());
	}
}
