package com.cusfit.cusfitbe.support.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface FaqControllerDocs {

	@Operation(summary = "Create Faq")
	ResponseEntity<ApiResponse<FaqResponse>> create(CreateFaqRequest request);

	@Operation(summary = "Get Faq")
	ResponseEntity<ApiResponse<FaqResponse>> getById(Long id);
}
