package com.cusfit.cusfitbe.terms.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Terms")
public interface TermsApiControllerDocs {

	@Operation(summary = "T1. 약관 목록", description = "종류별로 현재 시행 중인 최신 버전만 반환한다.")
	ResponseEntity<ApiResponse<List<TermsResponse>>> getCurrentTerms();

	@Operation(summary = "T2. 약관 상세")
	ResponseEntity<ApiResponse<TermsResponse>> getById(Long termsId);
}
