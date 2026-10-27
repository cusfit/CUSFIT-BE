package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;

public interface OptionPresetWriteService {

	OptionPresetResponse create(CreateOptionPresetRequest request);
}
