package com.cusfit.cusfitbe.terms.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.repository.TermsRepository;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TermsReadServiceImpl implements TermsReadService {

	private final TermsRepository termsRepository;

	public TermsReadServiceImpl(TermsRepository termsRepository) {
		this.termsRepository = termsRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public TermsResponse getById(Long id) {
		return termsRepository.findById(id)
				.map(TermsResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
