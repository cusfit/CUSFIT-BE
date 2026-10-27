package com.cusfit.cusfitbe.terms.service.impl;

import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.entity.Terms;
import com.cusfit.cusfitbe.terms.repository.TermsRepository;
import com.cusfit.cusfitbe.terms.service.TermsWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TermsWriteServiceImpl implements TermsWriteService {

	private final TermsRepository termsRepository;

	public TermsWriteServiceImpl(TermsRepository termsRepository) {
		this.termsRepository = termsRepository;
	}

	@Override
	@Transactional
	public TermsResponse create(CreateTermsRequest request) {
		Terms terms = Terms.create(request.type(), request.version(), request.contentUrl(), request.effectiveAt());
		return TermsResponse.from(termsRepository.save(terms));
	}
}
