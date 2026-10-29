package com.cusfit.cusfitbe.support.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.controller.docs.FaqControllerDocs;
import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.service.FaqReadService;
import com.cusfit.cusfitbe.support.service.FaqWriteService;
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
@RequestMapping("/api/dev/faqs")
public class FaqController implements FaqControllerDocs {

	private final FaqReadService readService;
	private final FaqWriteService writeService;

	public FaqController(FaqReadService readService, FaqWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<FaqResponse>> create(@Valid @RequestBody CreateFaqRequest request) {
		FaqResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/faqs/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<FaqResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
