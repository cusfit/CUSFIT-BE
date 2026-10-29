package com.cusfit.cusfitbe.support.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface InquiryControllerDocs {

	@Operation(summary = "Create Inquiry")
	ResponseEntity<ApiResponse<InquiryResponse>> create(CreateInquiryRequest request);

	@Operation(summary = "Get Inquiry")
	ResponseEntity<ApiResponse<InquiryResponse>> getById(Long id);
}
