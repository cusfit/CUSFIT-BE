package com.cusfit.cusfitbe.catalog.controller;

import com.cusfit.cusfitbe.catalog.controller.docs.OptionPresetControllerDocs;
import com.cusfit.cusfitbe.catalog.dto.request.CreateOptionPresetRequest;
import com.cusfit.cusfitbe.catalog.dto.response.OptionPresetResponse;
import com.cusfit.cusfitbe.catalog.service.OptionPresetReadService;
import com.cusfit.cusfitbe.catalog.service.OptionPresetWriteService;
import com.cusfit.cusfitbe.global.response.ApiResponse;
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
@RequestMapping("/api/dev/option-presets")
public class OptionPresetController implements OptionPresetControllerDocs {

	private final OptionPresetReadService readService;
	private final OptionPresetWriteService writeService;

	public OptionPresetController(OptionPresetReadService readService, OptionPresetWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<OptionPresetResponse>> create(@Valid @RequestBody CreateOptionPresetRequest request) {
		OptionPresetResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/option-presets/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<OptionPresetResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
