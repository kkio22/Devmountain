package nbc.devmountain.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.util.oauth2.CustomOAuth2UserService;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.user.dto.request.ProfileUpdateRequestDto;
import nbc.devmountain.domain.user.dto.request.UserLoginRequestDto;
import nbc.devmountain.domain.user.dto.request.UserRequestDto;
import nbc.devmountain.domain.user.dto.response.UserResponseDto;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final CustomOAuth2UserService customOAuth2UserService;

	@PostMapping("/signup")
	public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userRequestDto){
		return ResponseEntity.ok(userService.createUser(userRequestDto));
	}

	@PostMapping("/login")
	public ResponseEntity<UserResponseDto> loginUser(@RequestBody UserLoginRequestDto userLoginRequestDto, HttpServletRequest request) {
		User user = userService.loginUser(userLoginRequestDto);

		// 세션에 사용자 정보 저장
		SessionUser sessionUser = new SessionUser(user);
		request.getSession().setAttribute("user", sessionUser);

		// Spring Security 컨텍스트에 인증 정보 등록
		CustomUserPrincipal principal = new CustomUserPrincipal(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			principal,
			null,
			principal.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

		// 응답 반환
		return ResponseEntity.ok(UserResponseDto.from(user, user.getUserCategory()));
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal CustomUserPrincipal userDetails){
		System.out.println("CustomUserPrincipal 정보 확인 : " + userDetails.getName());
		return ResponseEntity.ok(userService.getUser(userDetails.getUserId()));
	}

	@GetMapping("/test")
	public String test(HttpServletRequest request) {
		SessionUser sessionUser = (SessionUser) request.getSession().getAttribute("user");
		if (sessionUser == null) {
			return "세션에 유저 정보 없음";
		}
		// System.out.println("OAuth2 로그인 사용자 이메일: " + sessionUser.getEmail());
		return "로그인 성공";
	}

	@PatchMapping("/me")
	public ResponseEntity<UserResponseDto> updateUser(
		@AuthenticationPrincipal CustomUserPrincipal userDetails,
		@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
		return ResponseEntity.ok(userService.updateUser(userDetails.getUserId(), profileUpdateRequestDto));
	}

	@DeleteMapping("/me")
	public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserPrincipal userDetails){
		return ResponseEntity.ok(userService.deleteUser(userDetails.getUserId()));
	}
}
