package com.cusfit.cusfitbe.terms.service;

import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import java.util.List;

public interface TermsReadService {
	TermsResponse getById(Long id);

	List<TermsResponse> getCurrentTerms();
}
