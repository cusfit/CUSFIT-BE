package com.cusfit.cusfitbe.support.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.controller.docs.SupportApiControllerDocs;
import com.cusfit.cusfitbe.support.dto.request.SubmitInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeSummaryResponse;
import com.cusfit.cusfitbe.support.service.FaqReadService;
import com.cusfit.cusfitbe.support.service.InquiryWriteService;
import com.cusfit.cusfitbe.support.service.NoticeReadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SupportApiController implements SupportApiControllerDocs {

	private final NoticeReadService noticeReadService;
	private final FaqReadService faqReadService;
	private final InquiryWriteService inquiryWriteService;

	public SupportApiController(NoticeReadService noticeReadService, FaqReadService faqReadService,
			InquiryWriteService inquiryWriteService) {
		this.noticeReadService = noticeReadService;
		this.faqReadService = faqReadService;
		this.inquiryWriteService = inquiryWriteService;
	}

	@Override
	@GetMapping("/notices")
	public ResponseEntity<ApiResponse<List<NoticeSummaryResponse>>> getNotices() {
		return ResponseEntity.ok(ApiResponse.ok(noticeReadService.getAll()));
	}

	@Override
	@GetMapping("/notices/{noticeId}")
	public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(@PathVariable Long noticeId) {
		return ResponseEntity.ok(ApiResponse.ok(noticeReadService.getById(noticeId)));
	}

	@Override
	@GetMapping("/faqs")
	public ResponseEntity<ApiResponse<List<FaqResponse>>> searchFaqs(@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(ApiResponse.ok(faqReadService.search(keyword)));
	}

	@Override
	@PostMapping(value = "/inquiries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Void>> submitInquiry(@AuthenticationPrincipal Jwt jwt,
			@Valid @ModelAttribute SubmitInquiryRequest request) {
		inquiryWriteService.submit(jwt.getSubject(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
	}
}
