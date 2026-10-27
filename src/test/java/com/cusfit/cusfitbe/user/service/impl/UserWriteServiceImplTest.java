package com.cusfit.cusfitbe.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.dto.request.CreateUserRequest;
import com.cusfit.cusfitbe.user.dto.response.UserResponse;
import com.cusfit.cusfitbe.user.entity.User;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserWriteServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserWriteServiceImpl userWriteService;

	@Test
	@DisplayName("사용자 생성 요청을 보내면 엔티티를 저장하고 저장된 사용자 응답을 반환한다")
	void create() {
		// given
		CreateUserRequest request = UserFixture.createRequest();
		given(userRepository.save(any(User.class)))
				.willAnswer(invocation -> UserFixture.persisted(invocation.getArgument(0), UserFixture.ID));

		// when
		UserResponse response = userWriteService.create(request);

		// then
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		then(userRepository).should().save(captor.capture());
		assertThat(captor.getValue().getNickname()).isEqualTo(UserFixture.NICKNAME);
		assertThat(response.id()).isEqualTo(UserFixture.ID);
		assertThat(response.nickname()).isEqualTo(UserFixture.NICKNAME);
		assertThat(response.createdAt()).isEqualTo(UserFixture.CREATED_AT);
	}

	@Test
	@DisplayName("닉네임이 공백인 생성 요청을 보내면 INVALID_REQUEST 예외가 발생하고 저장하지 않는다")
	void createWithBlankNickname() {
		// given
		CreateUserRequest request = UserFixture.createRequest(" ");

		// when
		BusinessException exception = assertThrows(BusinessException.class,
				() -> userWriteService.create(request));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
		then(userRepository).should(never()).save(any());
	}
}
