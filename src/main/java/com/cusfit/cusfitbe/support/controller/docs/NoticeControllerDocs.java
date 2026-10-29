package com.cusfit.cusfitbe.support.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface NoticeControllerDocs {

	@Operation(summary = "Create Notice")
	ResponseEntity<ApiResponse<NoticeResponse>> create(CreateNoticeRequest request);

	@Operation(summary = "Get Notice")
	ResponseEntity<ApiResponse<NoticeResponse>> getById(Long id);
}
