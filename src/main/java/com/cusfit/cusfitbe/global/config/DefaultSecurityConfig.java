package com.cusfit.cusfitbe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * local 외 프로필에서 /api/v1 밖의 경로를 막는다. {@link ApiSecurityConfig}가 SecurityFilterChain을 등록하면 Boot의 기본 체인이
 * 빠지므로, 이 체인이 없으면 나머지 경로가 인증 없이 열린다.
 */
@Configuration
@Profile("!local")
public class DefaultSecurityConfig {

	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
		return http.build();
	}
}
