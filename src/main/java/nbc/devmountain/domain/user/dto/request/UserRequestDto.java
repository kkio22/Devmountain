package nbc.devmountain.domain.user.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc.devmountain.domain.category.model.Category;
import nbc.devmountain.domain.user.model.User;

@Getter
@AllArgsConstructor
public class UserRequestDto {
	@NotBlank
	@Email
	private final String email;
	@NotBlank
	private final String password;
	@NotBlank
	private final String name;
	@NotBlank
	private final String phoneNumber;
	@NotBlank
	private final User.Role role;
	private final List<Category.CategoryName> category = new ArrayList<>();

}
