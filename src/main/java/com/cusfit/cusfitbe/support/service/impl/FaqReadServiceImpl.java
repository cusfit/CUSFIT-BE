package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.repository.FaqRepository;
import com.cusfit.cusfitbe.support.service.FaqReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FaqReadServiceImpl implements FaqReadService {

	private final FaqRepository repository;

	public FaqReadServiceImpl(FaqRepository repository) {
		this.repository = repository;
	}

	@Override
	public FaqResponse getById(Long id) {
		return repository.findById(id)
				.map(FaqResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
