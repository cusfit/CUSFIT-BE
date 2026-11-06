package com.cusfit.cusfitbe.support.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.controller.docs.SupportAdminControllerDocs;
import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.service.FaqWriteService;
import com.cusfit.cusfitbe.support.service.NoticeWriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class SupportAdminController implements SupportAdminControllerDocs {

	private final NoticeWriteService noticeWriteService;
	private final FaqWriteService faqWriteService;

	public SupportAdminController(NoticeWriteService noticeWriteService, FaqWriteService faqWriteService) {
		this.noticeWriteService = noticeWriteService;
		this.faqWriteService = faqWriteService;
	}

	@Override
	@PostMapping("/notices")
	public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(@Valid @RequestBody CreateNoticeRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(noticeWriteService.create(request)));
	}

	@Override
	@PostMapping("/faqs")
	public ResponseEntity<ApiResponse<FaqResponse>> createFaq(@Valid @RequestBody CreateFaqRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(faqWriteService.create(request)));
	}
}
