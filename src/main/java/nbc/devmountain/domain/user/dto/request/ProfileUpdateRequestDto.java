package nbc.devmountain.domain.user.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc.devmountain.domain.category.model.UserCategory;

@Getter
@AllArgsConstructor
public class ProfileUpdateRequestDto {
	private final String password;
	private final String name;
	private final String phoneNumber;
	private final List<UserCategory> userCategory;

}
