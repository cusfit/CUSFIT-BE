package com.cusfit.cusfitbe.support.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.controller.docs.NoticeControllerDocs;
import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.service.NoticeReadService;
import com.cusfit.cusfitbe.support.service.NoticeWriteService;
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
@RequestMapping("/api/dev/notices")
public class NoticeController implements NoticeControllerDocs {

	private final NoticeReadService readService;
	private final NoticeWriteService writeService;

	public NoticeController(NoticeReadService readService, NoticeWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<NoticeResponse>> create(@Valid @RequestBody CreateNoticeRequest request) {
		NoticeResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/notices/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<NoticeResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
