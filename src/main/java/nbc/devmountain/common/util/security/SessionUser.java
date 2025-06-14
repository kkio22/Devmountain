package nbc.devmountain.common.util.security;

import java.io.Serializable;

import lombok.Getter;
import nbc.devmountain.domain.user.model.User;

/*
 * 세션에 저장할 사용자 정보를 담는 DTO
 * */
@Getter
public class SessionUser implements Serializable {
	// private String name;
	private Long userId;
	private String email;
	private User.MembershipLevel membershipLevel;

	public SessionUser(User user) {
		// this.name = user.getName();
		this.userId = user.getUserId();
		this.email = user.getEmail();
		this.membershipLevel=user.getMembershipLevel();
	}
}
