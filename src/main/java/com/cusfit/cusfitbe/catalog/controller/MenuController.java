package com.cusfit.cusfitbe.catalog.controller;

import com.cusfit.cusfitbe.catalog.controller.docs.MenuControllerDocs;
import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.catalog.service.MenuWriteService;
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
@RequestMapping("/api/dev/menus")
public class MenuController implements MenuControllerDocs {

	private final MenuReadService readService;
	private final MenuWriteService writeService;

	public MenuController(MenuReadService readService, MenuWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<MenuResponse>> create(@Valid @RequestBody CreateMenuRequest request) {
		MenuResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/menus/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<MenuResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
