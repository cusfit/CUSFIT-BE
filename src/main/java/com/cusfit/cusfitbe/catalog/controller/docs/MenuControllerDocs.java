package com.cusfit.cusfitbe.catalog.controller.docs;

import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface MenuControllerDocs {

	@Operation(summary = "Create Menu")
	ResponseEntity<ApiResponse<MenuResponse>> create(CreateMenuRequest request);

	@Operation(summary = "Get Menu")
	ResponseEntity<ApiResponse<MenuResponse>> getById(Long id);
}
