package com.cusfit.cusfitbe.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserReadServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserReadServiceImpl userReadService;

	@Test
	@DisplayName("존재하는 사용자 ID로 조회하면 사용자 응답을 반환한다")
	void getById() {
		// given
		User user = UserFixture.persistedUser();
		given(userRepository.findById(UserFixture.ID)).willReturn(Optional.of(user));

		// when
		UserResponse response = userReadService.getById(UserFixture.ID);

		// then
		assertThat(response.id()).isEqualTo(UserFixture.ID);
		assertThat(response.nickname()).isEqualTo(UserFixture.NICKNAME);
		assertThat(response.createdAt()).isEqualTo(UserFixture.CREATED_AT);
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 조회하면 NOT_FOUND 예외가 발생한다")
	void getByIdNotFound() {
		// given
		Long missingId = 999L;
		given(userRepository.findById(missingId)).willReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> userReadService.getById(missingId));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}
}
