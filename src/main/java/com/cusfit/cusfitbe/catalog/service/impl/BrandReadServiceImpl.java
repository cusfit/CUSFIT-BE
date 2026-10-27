package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BrandReadServiceImpl implements BrandReadService {

	private final BrandRepository repository;

	public BrandReadServiceImpl(BrandRepository repository) {
		this.repository = repository;
	}

	@Override
	public BrandResponse getById(Long id) {
		return repository.findById(id)
				.map(BrandResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
