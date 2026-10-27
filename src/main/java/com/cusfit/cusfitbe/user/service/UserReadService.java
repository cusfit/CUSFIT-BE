package com.cusfit.cusfitbe.user.service;

import com.cusfit.cusfitbe.user.dto.response.UserResponse;

public interface UserReadService {

	UserResponse getById(Long id);
}
