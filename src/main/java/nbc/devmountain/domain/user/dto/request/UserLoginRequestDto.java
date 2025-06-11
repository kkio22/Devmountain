package nbc.devmountain.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginRequestDto {
	@NotBlank
	private final String email;
	@NotBlank
	private final String password;
}
