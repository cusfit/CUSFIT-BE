package com.cusfit.cusfitbe.support.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Support Admin")
public interface SupportAdminControllerDocs {

	@Operation(summary = "S5. 공지 등록 (관리자)")
	ResponseEntity<ApiResponse<NoticeResponse>> createNotice(CreateNoticeRequest request);

	@Operation(summary = "S5. FAQ 등록 (관리자)")
	ResponseEntity<ApiResponse<FaqResponse>> createFaq(CreateFaqRequest request);
}
