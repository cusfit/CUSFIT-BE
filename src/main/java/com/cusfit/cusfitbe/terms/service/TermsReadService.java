package com.cusfit.cusfitbe.terms.service;

import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;

public interface TermsReadService {
	TermsResponse getById(Long id);
}
