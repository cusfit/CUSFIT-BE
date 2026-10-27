package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;

public interface BrandReadService {

	BrandResponse getById(Long id);
}
