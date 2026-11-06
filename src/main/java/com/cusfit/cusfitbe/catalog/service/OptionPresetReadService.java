package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import java.util.List;

public interface OptionPresetReadService {

	OptionPresetResponse getById(Long id);

	List<OptionPresetResponse> getByBrandId(Long brandId);
}
