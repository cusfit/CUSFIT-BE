package com.cusfit.cusfitbe.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.cusfit.cusfitbe.testsupport.DatabaseCleaner;
import com.cusfit.cusfitbe.testsupport.ScenarioTest;
import com.cusfit.cusfitbe.user.fixture.UserFixture;
import com.cusfit.cusfitbe.user.repository.UserRepository;
import com.cusfit.cusfitbe.user.service.UserWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ScenarioTest
class ScenarioInfrastructureTest {

	@Autowired
	private UserWriteService userWriteService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DatabaseCleaner databaseCleaner;

	@Test
	@DisplayName("서비스로 만든 데이터는 커밋되어 조회되고, 정리하면 모두 비워진다")
	void commitsAndCleans() {
		// given
		Long userId = userWriteService.create(UserFixture.createRequest("시나리오유저")).id();

		// when & then
		assertThat(userRepository.findById(userId)).isPresent();

		databaseCleaner.clean();
		assertThat(userRepository.count()).isZero();
	}
}
