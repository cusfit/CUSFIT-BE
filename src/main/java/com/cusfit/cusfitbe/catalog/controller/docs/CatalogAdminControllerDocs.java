package com.cusfit.cusfitbe.catalog.controller.docs;

import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Catalog Admin")
public interface CatalogAdminControllerDocs {

	@Operation(summary = "C4. 브랜드 등록 (관리자)")
	ResponseEntity<ApiResponse<BrandResponse>> createBrand(CreateBrandRequest request);

	@Operation(summary = "C5. 메뉴 등록 (관리자)")
	ResponseEntity<ApiResponse<MenuResponse>> createMenu(CreateMenuRequest request);

	@Operation(summary = "C6. 옵션 프리셋 등록 (관리자)")
	ResponseEntity<ApiResponse<OptionPresetResponse>> createOptionPreset(CreateOptionPresetRequest request);
}
