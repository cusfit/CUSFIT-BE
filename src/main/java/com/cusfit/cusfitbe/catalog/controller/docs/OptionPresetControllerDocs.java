package com.cusfit.cusfitbe.catalog.controller.docs;

import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface OptionPresetControllerDocs {

	@Operation(summary = "Create OptionPreset")
	ResponseEntity<ApiResponse<OptionPresetResponse>> create(CreateOptionPresetRequest request);

	@Operation(summary = "Get OptionPreset")
	ResponseEntity<ApiResponse<OptionPresetResponse>> getById(Long id);
}
