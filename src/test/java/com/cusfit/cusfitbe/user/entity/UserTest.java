package com.cusfit.cusfitbe.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

	@Test
	@DisplayName("유효한 닉네임으로 생성하면 닉네임을 가진 사용자가 만들어진다")
	void create() {
		// given
		String nickname = UserFixture.NICKNAME;

		// when
		User user = User.create(nickname);

		// then
		assertThat(user).isInstanceOf(BaseCreatedEntity.class);
		assertThat(user.getNickname()).isEqualTo(nickname);
		assertThat(user.getId()).isNull();
		assertThat(user.getCreatedAt()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	@DisplayName("닉네임이 없거나 공백이면 INVALID_REQUEST 예외가 발생한다")
	void createWithBlankNickname(String nickname) {
		// given
		String invalid = nickname;

		// when
		BusinessException exception = assertThrows(BusinessException.class, () -> User.create(invalid));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
	}
}
