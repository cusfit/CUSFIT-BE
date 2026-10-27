package com.cusfit.cusfitbe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("local")
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/dev/**"))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/dev/**", "/swagger-ui.html", "/swagger-ui/**",
								"/api-docs", "/api-docs/**", "/actuator/health").permitAll()
						.anyRequest().denyAll());
		return http.build();
	}
}
