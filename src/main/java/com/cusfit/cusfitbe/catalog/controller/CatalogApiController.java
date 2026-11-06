package com.cusfit.cusfitbe.catalog.controller;

import com.cusfit.cusfitbe.catalog.controller.docs.CatalogApiControllerDocs;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogApiController implements CatalogApiControllerDocs {

	private final BrandReadService brandReadService;
	private final MenuReadService menuReadService;
	private final OptionPresetReadService optionPresetReadService;

	public CatalogApiController(BrandReadService brandReadService, MenuReadService menuReadService,
			OptionPresetReadService optionPresetReadService) {
		this.brandReadService = brandReadService;
		this.menuReadService = menuReadService;
		this.optionPresetReadService = optionPresetReadService;
	}

	@Override
	@GetMapping("/brands")
	public ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands() {
		return ResponseEntity.ok(ApiResponse.ok(brandReadService.getAll()));
	}

	@Override
	@GetMapping("/menus")
	public ResponseEntity<ApiResponse<List<MenuSummaryResponse>>> searchMenus(
			@RequestParam(required = false) Long brandId, @RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(ApiResponse.ok(menuReadService.search(brandId, keyword)));
	}

	@Override
	@GetMapping("/brands/{brandId}/option-presets")
	public ResponseEntity<ApiResponse<List<OptionPresetResponse>>> getOptionPresets(@PathVariable Long brandId) {
		return ResponseEntity.ok(ApiResponse.ok(optionPresetReadService.getByBrandId(brandId)));
	}
}
