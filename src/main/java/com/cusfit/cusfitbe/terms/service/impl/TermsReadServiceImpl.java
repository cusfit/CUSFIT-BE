package com.cusfit.cusfitbe.terms.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.repository.TermsRepository;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	@Override
	@Transactional(readOnly = true)
	public List<TermsResponse> getCurrentTerms() {
		// 종류별로 시행일이 가장 늦은 버전이 먼저 오므로 종류마다 첫 행만 남긴다.
		Map<String, TermsResponse> latestByType = new LinkedHashMap<>();
		termsRepository.findByEffectiveAtLessThanEqualOrderByTypeAscEffectiveAtDesc(Instant.now())
				.forEach(terms -> latestByType.putIfAbsent(terms.getType(), TermsResponse.from(terms)));
		return List.copyOf(latestByType.values());
	}
}
