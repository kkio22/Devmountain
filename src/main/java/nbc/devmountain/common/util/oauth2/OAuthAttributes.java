package nbc.devmountain.common.util.oauth2;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.user.model.User;

/*
* OAuth2(Google, Naver 등)에서 가져온 사용자 정보 변환
* */
@Getter
public class OAuthAttributes {
	private Map<String, Object> attributes; // OAuth2에서 받은 사용자 전체 정보
	private String attributeKey; // 사용자 식별에 사용할 key 값
	private String name;
	private String email;

	@Builder
	public OAuthAttributes(Map<String, Object> attributes,
		String attributeKey, String name,
		String email) {
		this.attributes = attributes;
		this.attributeKey= attributeKey;
		this.name = name;
		this.email = email;
	}

	/*
	 * OAuth2 별로 사용자 정보 매핑
	 * 현재 Google만 사용중 (추후 Naver, Kakao 등 추가 예정)
	 */
	public static OAuthAttributes of(String registrationId, String userNameAttribute, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return ofGoogle(userNameAttribute, attributes);
		} //else if ("kakao".equals(registrationId)) {
		// 	return ofKakao(userNameAttributeName, attributes);
		// }
		throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
	}


	// 구글 사용자 정보 매핑 처리
	private static OAuthAttributes ofGoogle(String userNameAttribute,
		Map<String, Object> attributes) {
		return OAuthAttributes.builder()
			.name((String) attributes.get("name"))
			.email((String) attributes.get("email"))
			.attributes(attributes)
			.attributeKey(userNameAttribute)
			.build();
	}

	// OAuth2에서 받은 정보를 바탕으로 User 생성
	public User toEntity() {
		return User.builder()
			.name(name)
			.email(email)
			.password("0000") // 임시 비번 (OAuth 로그인 사용자는 로그인에 사용하지 않음)
			.loginType(User.LoginType.GOOGLE)
			.membershipLevel(User.MembershipLevel.FREE)
			.role(User.Role.USER)
			.build();
	}
}
