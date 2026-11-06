package com.cusfit.cusfitbe.user.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.user.dto.request.NicknameAvailabilityRequest;
import com.cusfit.cusfitbe.user.dto.request.SignUpRequest;
import com.cusfit.cusfitbe.user.dto.request.UpdateProfileRequest;
import com.cusfit.cusfitbe.user.dto.request.WithdrawRequest;
import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.NicknameAvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "User")
public interface UserApiControllerDocs {

	@Operation(summary = "U1. 닉네임 사용 가능 여부", description = "형식이 틀리면 400, 중복이면 available=false.")
	ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(NicknameAvailabilityRequest request);

	@Operation(summary = "U2. 회원가입 (미구현)")
	ResponseEntity<ApiResponse<MyProfileResponse>> signUp(Jwt jwt, SignUpRequest request);

	@Operation(summary = "U3. 내 정보 (미구현)")
	ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(Jwt jwt);

	@Operation(summary = "U4. 프로필 수정 (미구현)")
	ResponseEntity<ApiResponse<MyProfileResponse>> updateProfile(Jwt jwt, UpdateProfileRequest request);

	@Operation(summary = "U5. 회원 탈퇴 (미구현)")
	ResponseEntity<Void> withdraw(Jwt jwt, WithdrawRequest request);
}
