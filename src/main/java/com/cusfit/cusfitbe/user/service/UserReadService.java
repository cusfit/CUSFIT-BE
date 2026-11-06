package com.cusfit.cusfitbe.user.service;

import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.NicknameAvailabilityResponse;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;

public interface UserReadService {

	UserResponse getById(Long id);

	NicknameAvailabilityResponse checkNicknameAvailability(String nickname);

	MyProfileResponse getMyProfile(String authSubject);
}
