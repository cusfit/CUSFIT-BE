package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;

public interface BrandWriteService {

	BrandResponse create(CreateBrandRequest request);
}
