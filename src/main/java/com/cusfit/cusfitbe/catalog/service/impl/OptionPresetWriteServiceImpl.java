package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OptionPresetWriteServiceImpl implements OptionPresetWriteService {

	private final OptionPresetRepository repository;
	private final BrandReadService brandReadService;

	public OptionPresetWriteServiceImpl(OptionPresetRepository repository, BrandReadService brandReadService) {
		this.repository = repository;
		this.brandReadService = brandReadService;
	}

	@Override
	public OptionPresetResponse create(CreateOptionPresetRequest request) {
		brandReadService.getById(request.brandId());
		OptionPreset entity = OptionPreset.create(request.brandId(), request.name());
		return OptionPresetResponse.from(repository.save(entity));
	}
}
