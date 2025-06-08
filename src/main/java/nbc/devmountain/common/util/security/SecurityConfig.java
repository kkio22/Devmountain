package nbc.devmountain.common.util.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import nbc.devmountain.common.util.oauth2.CustomOAuth2UserService;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws
		Exception {
		http
			.csrf().disable() //csrf 비활성화
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // 세션 기반 인증 사용
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/users/signup","/users/login", "/login").permitAll() // 인증이 필요없는 부분 추가 예정
				.anyRequest().authenticated()
			)
			// OAuth2 설정
			.oauth2Login(oauth -> oauth
				// .loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)) // 사용자 정보 처리
				.defaultSuccessUrl("/users/test") // 로그인 성공 후 이동할 페이지
			)
			.formLogin(form -> form.disable());

		return http.build();
	}

	// 인증 매니저 Bean 등록
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	// 사용자 인증 정보 불러오기
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return username -> {
			// email 로 조회
			User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			return new CustomUserPrincipal(user);
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
