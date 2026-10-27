package com.cusfit.cusfitbe.terms.service;

import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;

public interface TermsWriteService {
	TermsResponse create(CreateTermsRequest request);
}
