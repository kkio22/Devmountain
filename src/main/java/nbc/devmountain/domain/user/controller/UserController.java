package nbc.devmountain.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.user.dto.request.ProfileUpdateRequestDto;
import nbc.devmountain.domain.user.dto.request.UserRequestDto;
import nbc.devmountain.domain.user.dto.response.UserResponseDto;
import nbc.devmountain.domain.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userRequestDto){
		return ResponseEntity.ok(userService.createUser(userRequestDto));
	}

	// @PostMapping("/login")
	// public ResponseEntity<String> loginUser(@RequestBody UserLoginRequestDto userLoginRequestDto){
	// 	return ResponseEntity.ok(userService.loginUser(userLoginRequestDto));
	// }

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId){
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(
		@PathVariable Long userId,
		@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
		return ResponseEntity.ok(userService.updateUser(userId, profileUpdateRequestDto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long userId){
		return ResponseEntity.ok(userService.deleteUser(userId));
	}
}
