package com.cusfit.cusfitbe.terms.fixture;

import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.entity.Terms;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class TermsFixture {

	public static final String TYPE = "SERVICE";
	public static final String VERSION = "1.0";
	public static final String CONTENT_URL = "https://cusfit.example/terms/service/1.0";
	public static final Instant EFFECTIVE_AT = Instant.parse("2026-01-01T00:00:00Z");
	public static final Instant CREATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	private TermsFixture() {
	}

	public static Terms terms() {
		return Terms.create(TYPE, VERSION, CONTENT_URL, EFFECTIVE_AT);
	}

	public static Terms persistedTerms(Long id) {
		Terms terms = terms();
		ReflectionTestUtils.setField(terms, "id", id);
		ReflectionTestUtils.setField(terms, "createdAt", CREATED_AT);
		return terms;
	}

	public static CreateTermsRequest createRequest() {
		return new CreateTermsRequest(TYPE, VERSION, CONTENT_URL, EFFECTIVE_AT);
	}

	public static CreateTermsRequest createRequestWithType(String type) {
		return new CreateTermsRequest(type, VERSION, CONTENT_URL, EFFECTIVE_AT);
	}

	public static CreateTermsRequest createRequestWithEffectiveAt(Instant effectiveAt) {
		return new CreateTermsRequest(TYPE, VERSION, CONTENT_URL, effectiveAt);
	}

	public static TermsResponse response(Long id) {
		return new TermsResponse(id, TYPE, VERSION, CONTENT_URL, EFFECTIVE_AT, CREATED_AT);
	}
}
