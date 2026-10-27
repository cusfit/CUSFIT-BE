package com.cusfit.cusfitbe.user.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
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
}
