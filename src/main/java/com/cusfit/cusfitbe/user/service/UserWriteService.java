package com.cusfit.cusfitbe.user.service;

import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;

public interface UserWriteService {

	UserResponse create(CreateUserRequest request);
}
