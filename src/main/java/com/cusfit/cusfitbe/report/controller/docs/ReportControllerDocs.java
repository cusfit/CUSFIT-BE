package com.cusfit.cusfitbe.report.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface ReportControllerDocs {

	@Operation(summary = "신고 생성")
	ResponseEntity<ApiResponse<ReportResponse>> create(CreateReportRequest request);

	@Operation(summary = "신고 단건 조회")
	ResponseEntity<ApiResponse<ReportResponse>> getById(Long id);
}
