package com.cusfit.cusfitbe.terms.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.terms.controller.docs.TermsControllerDocs;
import com.cusfit.cusfitbe.terms.dto.request.CreateTermsRequest;
import com.cusfit.cusfitbe.terms.dto.response.TermsResponse;
import com.cusfit.cusfitbe.terms.service.TermsReadService;
import com.cusfit.cusfitbe.terms.service.TermsWriteService;
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
@RequestMapping("/api/dev/terms")
public class TermsController implements TermsControllerDocs {

	private final TermsReadService termsReadService;
	private final TermsWriteService termsWriteService;

	public TermsController(TermsReadService termsReadService, TermsWriteService termsWriteService) {
		this.termsReadService = termsReadService;
		this.termsWriteService = termsWriteService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<TermsResponse>> create(@Valid @RequestBody CreateTermsRequest request) {
		TermsResponse response = termsWriteService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/terms/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TermsResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(termsReadService.getById(id)));
	}
}
