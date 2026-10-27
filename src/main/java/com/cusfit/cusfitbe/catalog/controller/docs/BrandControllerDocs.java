package com.cusfit.cusfitbe.catalog.controller.docs;

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface BrandControllerDocs {

	@Operation(summary = "Create Brand")
	ResponseEntity<ApiResponse<BrandResponse>> create(CreateBrandRequest request);

	@Operation(summary = "Get Brand")
	ResponseEntity<ApiResponse<BrandResponse>> getById(Long id);
}
