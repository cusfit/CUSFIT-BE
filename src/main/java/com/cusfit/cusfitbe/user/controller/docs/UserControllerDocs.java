package com.cusfit.cusfitbe.user.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface UserControllerDocs {

	@Operation(summary = "사용자 생성")
	ResponseEntity<ApiResponse<UserResponse>> create(CreateUserRequest request);

	@Operation(summary = "사용자 단건 조회")
	ResponseEntity<ApiResponse<UserResponse>> getById(Long id);
}
