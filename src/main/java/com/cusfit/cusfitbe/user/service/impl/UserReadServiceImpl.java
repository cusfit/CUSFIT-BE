package com.cusfit.cusfitbe.user.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.dto.response.MyProfileResponse;
import com.cusfit.cusfitbe.user.dto.response.NicknameAvailabilityResponse;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserReadServiceImpl implements UserReadService {

	private final UserRepository userRepository;

	public UserReadServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserResponse getById(Long id) {
		return userRepository.findById(id)
				.map(UserResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}

	@Override
	public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
		// 대소문자 무시 중복(U-02 추천)은 정책이 확정되면 반영한다. 지금은 저장된 값과 정확히 같을 때만 중복이다.
		return new NicknameAvailabilityResponse(!userRepository.existsByNickname(nickname));
	}

	@Override
	public MyProfileResponse getMyProfile(String authSubject) {
		// 토큰 subject와 회원을 잇는 컬럼(D1)과 이메일 컬럼이 생기면 구현한다.
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
