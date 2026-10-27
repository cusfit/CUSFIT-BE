package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OptionPresetReadServiceImpl implements OptionPresetReadService {

	private final OptionPresetRepository repository;

	public OptionPresetReadServiceImpl(OptionPresetRepository repository) {
		this.repository = repository;
	}

	@Override
	public OptionPresetResponse getById(Long id) {
		return repository.findById(id)
				.map(OptionPresetResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
