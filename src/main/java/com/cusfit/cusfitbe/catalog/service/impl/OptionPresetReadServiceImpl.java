package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.repository.OptionPresetRepository;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OptionPresetReadServiceImpl implements OptionPresetReadService {

	private final OptionPresetRepository repository;
	private final BrandRepository brandRepository;

	public OptionPresetReadServiceImpl(OptionPresetRepository repository, BrandRepository brandRepository) {
		this.repository = repository;
		this.brandRepository = brandRepository;
	}

	@Override
	public OptionPresetResponse getById(Long id) {
		return repository.findById(id)
				.map(OptionPresetResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}

	@Override
	public List<OptionPresetResponse> getByBrandId(Long brandId) {
		if (!brandRepository.existsById(brandId)) {
			throw new BusinessException(ErrorCode.NOT_FOUND);
		}
		return repository.findByBrandIdOrderByIdAsc(brandId).stream().map(OptionPresetResponse::from).toList();
	}
}
