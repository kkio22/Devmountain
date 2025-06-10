package nbc.devmountain.domain.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.category.model.Category;
import nbc.devmountain.domain.category.model.UserCategory;
import nbc.devmountain.domain.category.repository.CategoryRepository;
import nbc.devmountain.domain.category.repository.UserCategoryRepository;
import nbc.devmountain.domain.user.dto.request.ProfileUpdateRequestDto;
import nbc.devmountain.domain.user.dto.request.UserLoginRequestDto;
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

	@Transactional
	public UserResponseDto createUser(UserRequestDto userRequestDto) {
		// 중복 이메일 예외 처리
		if (userRepository.existsByEmail(userRequestDto.getEmail())) {
			throw new RuntimeException("이미 존재하는 사용자입니다.");
		}

		String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());

		// 사용자 저장
		User user = User.builder()
			.email(userRequestDto.getEmail())
			.password(encodedPassword)
			.name(userRequestDto.getName())
			.phoneNumber(userRequestDto.getPhoneNumber())
			// userRequestDto에서 받아올 수 있도록 추후 수정
			.role(User.Role.USER)
			.membershipLevel(User.MembershipLevel.FREE)
			.loginType(User.LoginType.EMAIL)
			.build();

		userRepository.save(user);

		// 카테고리 저장
		List<Category> categories = userRequestDto.getCategory().stream()
			.map(name -> categoryRepository.findByName(name)
				.orElseThrow(() -> new RuntimeException("Category not found: " + name)))
			.toList();
		if (categories != null) {
			for (Category category : categories) {
				UserCategory userCategory = UserCategory.builder()
					.user(user)
					.category(category)
					.build();
				userCategoryRepository.save(userCategory);
			}
		}

		return UserResponseDto.from(user, user.getUserCategory());
	}

	public User loginUser(UserLoginRequestDto userLoginRequestDto) {
		User user = userRepository.findByEmail(userLoginRequestDto.getEmail())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

		if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
		}

		return user;
	}


	// @Transactional(readOnly = true)
	public UserResponseDto getUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return UserResponseDto.from(user, user.getUserCategory());
	}

	public UserResponseDto updateUser(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		// 비밀번호: null이 아니면 암호화해서 수정
		if (profileUpdateRequestDto.getPassword() != null && !profileUpdateRequestDto.getPassword().isBlank()) {
			String encodedPassword = passwordEncoder.encode(profileUpdateRequestDto.getPassword());
			user.updatePassword(encodedPassword);
		}

		// 이름
		if (profileUpdateRequestDto.getName() != null) {
			user.updateName(profileUpdateRequestDto.getName());
		}

		// 전화번호
		if (profileUpdateRequestDto.getPhoneNumber() != null) {
			user.updatePhoneNumber(profileUpdateRequestDto.getPhoneNumber());
		}

		// 관심 카테고리 전체 교체
		if (profileUpdateRequestDto.getCategory() != null) {
			List<UserCategory> userCategories = profileUpdateRequestDto.getCategory().stream()
				.map(categoryName -> {
					Category category = categoryRepository.findByName(categoryName)
						.orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryName));
					return new UserCategory(user, category);
				})
				.toList();

			user.updateCategories(userCategories);
		}

		return UserResponseDto.from(user, user.getUserCategory());
	}


	public String deleteUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		userRepository.delete(user);
		return "회원 탈퇴되었습니다.";
	}

}
