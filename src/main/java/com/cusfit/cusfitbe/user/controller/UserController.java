package com.cusfit.cusfitbe.user.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.user.controller.docs.UserControllerDocs;
import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.service.UserReadService;
import com.cusfit.cusfitbe.user.service.UserWriteService;
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
@RequestMapping("/api/dev/users")
public class UserController implements UserControllerDocs {

	private final UserReadService userReadService;
	private final UserWriteService userWriteService;

	public UserController(UserReadService userReadService, UserWriteService userWriteService) {
		this.userReadService = userReadService;
		this.userWriteService = userWriteService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
		UserResponse response = userWriteService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/users/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(userReadService.getById(id)));
	}
}
