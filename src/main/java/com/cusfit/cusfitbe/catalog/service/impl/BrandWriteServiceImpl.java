package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.entity.Brand;
import com.cusfit.cusfitbe.catalog.repository.BrandRepository;
import com.cusfit.cusfitbe.catalog.service.BrandWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandWriteServiceImpl implements BrandWriteService {

	private final BrandRepository repository;

	public BrandWriteServiceImpl(BrandRepository repository) {
		this.repository = repository;
	}

	@Override
	public BrandResponse create(CreateBrandRequest request) {
		Brand entity = Brand.create(request.name());
		return BrandResponse.from(repository.save(entity));
	}
}
