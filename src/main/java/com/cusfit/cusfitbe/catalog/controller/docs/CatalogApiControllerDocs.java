package com.cusfit.cusfitbe.catalog.controller.docs;

import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Catalog")
public interface CatalogApiControllerDocs {

	@Operation(summary = "C1. 브랜드 목록")
	ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands();

	@Operation(summary = "C2. 메뉴 검색", description = "keyword는 브랜드명 또는 메뉴명 부분 일치. 카테고리·판매 여부는 스키마 추가 후 지원한다.")
	ResponseEntity<ApiResponse<List<MenuSummaryResponse>>> searchMenus(Long brandId, String keyword);

	@Operation(summary = "C3. 옵션 프리셋 목록", description = "group 필터는 option_presets에 그룹 컬럼이 생기면 지원한다.")
	ResponseEntity<ApiResponse<List<OptionPresetResponse>>> getOptionPresets(Long brandId);
}
