package com.cusfit.cusfitbe.report.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.report.controller.docs.ReportControllerDocs;
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.service.ReportReadService;
import com.cusfit.cusfitbe.report.service.ReportWriteService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/api/dev/reports")
public class ReportController implements ReportControllerDocs {

	private final ReportReadService reportReadService;
	private final ReportWriteService reportWriteService;

	public ReportController(ReportReadService reportReadService, ReportWriteService reportWriteService) {
		this.reportReadService = reportReadService;
		this.reportWriteService = reportWriteService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<ReportResponse>> create(@Valid @RequestBody CreateReportRequest request) {
		ReportResponse response = reportWriteService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/reports/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ReportResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(reportReadService.getById(id)));
	}
}
