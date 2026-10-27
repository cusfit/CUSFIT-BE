package com.cusfit.cusfitbe.user.service.impl;

import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
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
}
