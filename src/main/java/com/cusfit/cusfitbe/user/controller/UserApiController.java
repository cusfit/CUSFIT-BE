package com.cusfit.cusfitbe.user.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.user.controller.docs.UserApiControllerDocs;
import com.cusfit.cusfitbe.user.dto.request.NicknameAvailabilityRequest;
import com.cusfit.cusfitbe.user.dto.request.SignUpRequest;
import com.cusfit.cusfitbe.user.dto.request.UpdateProfileRequest;
import com.cusfit.cusfitbe.user.dto.request.WithdrawRequest;
import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.NicknameAvailabilityResponse;
import com.cusfit.cusfitbe.user.service.UserReadService;
import com.cusfit.cusfitbe.user.service.UserWriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserApiController implements UserApiControllerDocs {

	private final UserReadService readService;
	private final UserWriteService writeService;

	public UserApiController(UserReadService readService, UserWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@GetMapping("/nickname/availability")
	public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
			@Valid @ModelAttribute NicknameAvailabilityRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(readService.checkNicknameAvailability(request.nickname())));
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<MyProfileResponse>> signUp(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody SignUpRequest request) {
		MyProfileResponse response = writeService.signUp(jwt.getSubject(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getMyProfile(jwt.getSubject())));
	}

	@Override
	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<MyProfileResponse>> updateProfile(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(writeService.updateProfile(jwt.getSubject(), request)));
	}

	@Override
	@DeleteMapping("/me")
	public ResponseEntity<Void> withdraw(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody WithdrawRequest request) {
		writeService.withdraw(jwt.getSubject(), request);
		return ResponseEntity.noContent().build();
	}
}
