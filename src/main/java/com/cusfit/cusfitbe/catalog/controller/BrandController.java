package com.cusfit.cusfitbe.catalog.controller;

import com.cusfit.cusfitbe.catalog.controller.docs.BrandControllerDocs;
import com.cusfit.cusfitbe.catalog.dto.request.CreateBrandRequest;
import com.cusfit.cusfitbe.catalog.dto.response.BrandResponse;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.BrandWriteService;
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
@RequestMapping("/api/dev/brands")
public class BrandController implements BrandControllerDocs {

	private final BrandReadService readService;
	private final BrandWriteService writeService;

	public BrandController(BrandReadService readService, BrandWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody CreateBrandRequest request) {
		BrandResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/brands/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BrandResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
