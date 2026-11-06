package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import java.util.List;

public interface BrandReadService {

	BrandResponse getById(Long id);

	List<BrandResponse> getAll();
}
