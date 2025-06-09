package nbc.devmountain.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.category.model.UserCategory;
import nbc.devmountain.domain.user.model.User;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {
	private final Long userId;
	private final String email;
	private final String password;
	private final String name;
	private final String phoneNumber;
	private final User.Role role;
	private final User.MembershipLevel membership;
	private final LocalDateTime createdAt;
	private final List<UserCategory> category;

	public static UserResponseDto from(User user, List<UserCategory> category) {
		return UserResponseDto.builder()
			.userId(user.getUserId())
			.email(user.getEmail())
			.password(user.getPassword())
			.name(user.getName())
			.phoneNumber(user.getPhoneNumber())
			.role(user.getRole())
			.membership(user.getMembershipLevel())
			.createdAt(user.getCreatedAt())
			.category(category)
			.build();
	}
}
