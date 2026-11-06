package com.cusfit.cusfitbe.user.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.request.SignUpRequest;
import com.cusfit.cusfitbe.user.dto.request.UpdateProfileRequest;
import com.cusfit.cusfitbe.user.dto.request.WithdrawRequest;
import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import com.cusfit.cusfitbe.user.service.UserWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserWriteServiceImpl implements UserWriteService {

	private final UserRepository userRepository;

	public UserWriteServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserResponse create(CreateUserRequest request) {
		User user = User.create(request.nickname());
		return UserResponse.from(userRepository.save(user));
	}

	// 아래 세 기능은 토큰 subject와 회원을 잇는 컬럼(D1), 약관 동의 이력, 탈퇴 상태가 스키마에 생기면 구현한다.

	@Override
	public MyProfileResponse signUp(String authSubject, SignUpRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public MyProfileResponse updateProfile(String authSubject, UpdateProfileRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public void withdraw(String authSubject, WithdrawRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
