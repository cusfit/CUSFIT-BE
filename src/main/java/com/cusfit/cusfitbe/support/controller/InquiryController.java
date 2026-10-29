package com.cusfit.cusfitbe.support.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.controller.docs.InquiryControllerDocs;
import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.service.InquiryReadService;
import com.cusfit.cusfitbe.support.service.InquiryWriteService;
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
@RequestMapping("/api/dev/inquiries")
public class InquiryController implements InquiryControllerDocs {

	private final InquiryReadService readService;
	private final InquiryWriteService writeService;

	public InquiryController(InquiryReadService readService, InquiryWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<InquiryResponse>> create(@Valid @RequestBody CreateInquiryRequest request) {
		InquiryResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/inquiries/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<InquiryResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
