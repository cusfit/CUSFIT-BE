package com.cusfit.cusfitbe.terms.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface TermsControllerDocs {

	@Operation(summary = "약관 생성")
	ResponseEntity<ApiResponse<TermsResponse>> create(CreateTermsRequest request);

	@Operation(summary = "약관 단건 조회")
	ResponseEntity<ApiResponse<TermsResponse>> getById(Long id);
}
