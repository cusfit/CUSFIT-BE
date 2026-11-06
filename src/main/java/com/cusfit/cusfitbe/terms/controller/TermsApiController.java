package com.cusfit.cusfitbe.terms.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.terms.controller.docs.TermsApiControllerDocs;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
public class TermsApiController implements TermsApiControllerDocs {

	private final TermsReadService readService;

	public TermsApiController(TermsReadService readService) {
		this.readService = readService;
	}

	@Override
	@GetMapping
	public ResponseEntity<ApiResponse<List<TermsResponse>>> getCurrentTerms() {
		return ResponseEntity.ok(ApiResponse.ok(readService.getCurrentTerms()));
	}

	@Override
	@GetMapping("/{termsId}")
	public ResponseEntity<ApiResponse<TermsResponse>> getById(@PathVariable Long termsId) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(termsId)));
	}
}
