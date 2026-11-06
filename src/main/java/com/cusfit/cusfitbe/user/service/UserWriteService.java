package com.cusfit.cusfitbe.user.service;

import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.request.SignUpRequest;
import com.cusfit.cusfitbe.user.dto.request.UpdateProfileRequest;
import com.cusfit.cusfitbe.user.dto.request.WithdrawRequest;
import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;

public interface UserWriteService {

	UserResponse create(CreateUserRequest request);

	MyProfileResponse signUp(String authSubject, SignUpRequest request);

	MyProfileResponse updateProfile(String authSubject, UpdateProfileRequest request);

	void withdraw(String authSubject, WithdrawRequest request);
}
