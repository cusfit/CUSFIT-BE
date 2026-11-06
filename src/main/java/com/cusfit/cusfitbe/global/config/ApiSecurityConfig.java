package com.cusfit.cusfitbe.global.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** 운영 API(/api/v1/**)의 인증 규칙. 공개 범위는 docs/API_DOCS.md의 인증 열과 D2 가정을 따른다. */
@Configuration
public class ApiSecurityConfig {

	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> jwtDecoder)
			throws Exception {
		http
				.securityMatcher("/api/v1/**")
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/v1/terms/**", "/api/v1/brands/**", "/api/v1/menus/**",
								"/api/v1/notices/**", "/api/v1/faqs/**", "/api/v1/recipes", "/api/v1/recipes/*")
						.permitAll()
						.anyRequest().authenticated())
				// OAUTH2_ISSUER_URI가 비어 있으면(local, test) Boot가 디코더를 만들지 않으므로 모든 토큰을 거부한다.
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder.getIfAvailable(
						() -> token -> {
							throw new BadJwtException("JWT issuer is not configured");
						}))));
		return http.build();
	}
}
