package nbc.devmountain.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.category.model.Category;
import nbc.devmountain.domain.category.model.UserCategory;
import nbc.devmountain.domain.category.repository.CategoryRepository;
import nbc.devmountain.domain.category.repository.UserCategoryRepository;
import nbc.devmountain.domain.user.dto.request.ProfileUpdateRequestDto;
import nbc.devmountain.domain.user.dto.request.UserRequestDto;
import nbc.devmountain.domain.user.dto.response.UserResponseDto;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final UserCategoryRepository userCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final PasswordEncoder passwordEncoder;

	public UserResponseDto createUser(UserRequestDto userRequestDto) {
		// 중복 이메일 예외 처리
		if (userRepository.existsByEmail(userRequestDto.getEmail())) {
			throw new RuntimeException("이미 존재하는 사용자입니다.");
		}
		// 사용자 저장
		User user = User.builder()
			.email(userRequestDto.getEmail())
			.password(userRequestDto.getPassword())
			.name(userRequestDto.getName())
			.phoneNumber(userRequestDto.getPhoneNumber())
			.role(userRequestDto.getRole())
			.membershipLevel(User.MembershipLevel.FREE)
			.build();

		userRepository.save(user);

		// 카테고리 저장
		for (Category category : userRequestDto.getCategory()) {
			UserCategory userCategory = UserCategory.builder()
				.user(user)
				.category(categoryRepository.findById(category.getCategoryId())
					.orElseThrow(() -> new IllegalArgumentException("Category not found")))
				.build();
			userCategoryRepository.save(userCategory);
		}

		return UserResponseDto.from(user, user.getUserCategory()); // category 목록 추가 예정
	}

	@Transactional(readOnly = true)
	public UserResponseDto getUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return UserResponseDto.from(user, user.getUserCategory());
	}

	public UserResponseDto updateUser(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (!passwordEncoder.matches(profileUpdateRequestDto.getPassword(), user.getPassword())) {
			throw new RuntimeException("비밀번호가 다릅니다.");
		}

		user.updateProfile(
			profileUpdateRequestDto.getPassword(),
			profileUpdateRequestDto.getName(),
			profileUpdateRequestDto.getPhoneNumber(),
			profileUpdateRequestDto.getUserCategory());
		return UserResponseDto.from(user, user.getUserCategory());
	}

	public String deleteUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		userRepository.delete(user);
		return "회원 탈퇴되었습니다.";
	}

}
