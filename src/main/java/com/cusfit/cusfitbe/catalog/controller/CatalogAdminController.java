package com.cusfit.cusfitbe.catalog.controller;

import com.cusfit.cusfitbe.catalog.controller.docs.CatalogAdminControllerDocs;
import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.service.BrandWriteService;
import com.cusfit.cusfitbe.catalog.service.MenuWriteService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetWriteService;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class CatalogAdminController implements CatalogAdminControllerDocs {

	private final BrandWriteService brandWriteService;
	private final MenuWriteService menuWriteService;
	private final OptionPresetWriteService optionPresetWriteService;

	public CatalogAdminController(BrandWriteService brandWriteService, MenuWriteService menuWriteService,
			OptionPresetWriteService optionPresetWriteService) {
		this.brandWriteService = brandWriteService;
		this.menuWriteService = menuWriteService;
		this.optionPresetWriteService = optionPresetWriteService;
	}

	@Override
	@PostMapping("/brands")
	public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody CreateBrandRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(brandWriteService.create(request)));
	}

	@Override
	@PostMapping("/menus")
	public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody CreateMenuRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(menuWriteService.create(request)));
	}

	@Override
	@PostMapping("/option-presets")
	public ResponseEntity<ApiResponse<OptionPresetResponse>> createOptionPreset(
			@Valid @RequestBody CreateOptionPresetRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(optionPresetWriteService.create(request)));
	}
}
