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
	private User.LoginType loginType;

	@Builder
	public OAuthAttributes(Map<String, Object> attributes, String attributeKey,
		String name, String email, User.LoginType loginType) {
		this.attributes = attributes;
		this.attributeKey = attributeKey;
		this.name = name;
		this.email = email;
		this.loginType = loginType;
	}

	/*
	 * OAuth2 별로 사용자 정보 매핑
	 * 현재 Google만 사용중 (추후 Naver, Kakao 등 추가 예정)
	 */
	public static OAuthAttributes of(String registrationId, String userNameAttribute, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return ofGoogle(userNameAttribute, attributes);
			// } else if ("kakao".equals(registrationId)) {
			// 	return ofKakao(userNameAttribute, attributes);
		} else if ("naver".equals(registrationId)) {
			return ofNaver(userNameAttribute, attributes);
		}
		throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
	}

	// /oauth2/authorization/google
	// 구글 사용자 정보 매핑 처리
	private static OAuthAttributes ofGoogle(String userNameAttribute,
		Map<String, Object> attributes) {
		return OAuthAttributes.builder()
			.name((String)attributes.get("name"))
			.email((String)attributes.get("email"))
			.attributes(attributes)
			.attributeKey(userNameAttribute)
			.loginType(User.LoginType.GOOGLE)
			.build();
	}

	// /oauth2/authorization/naver
	// naver 사용자 매핑
	private static OAuthAttributes ofNaver(String userNameAttribute,
		Map<String, Object> attributes) {

		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		return OAuthAttributes.builder()
			.name((String)response.get("name"))
			.email((String)response.get("email"))
			.attributes(response) // 또는 전체 attributes 저장
			.attributeKey(userNameAttribute)
			.loginType(User.LoginType.NAVER)
			.build();
	}

	// /oauth2/authorization/kakao
	// kakao 사용자 매핑
	// 카카오에서 email을 가져오려면 사업자 정보 등록해야함
	// private static OAuthAttributes ofKakao(String userNameAttribute,
	// 	Map<String, Object> attributes) {
	//
	// 	Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
	// 	Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
	//
	// 	return OAuthAttributes.builder()
	// 		.name((String) profile.get("nickname"))
	// 		.attributes(attributes)
	// 		.attributeKey(userNameAttribute)
	// 		.loginType(User.LoginType.KAKAO)
	// 		.build();
	// }

	// OAuth2에서 받은 정보를 바탕으로 User 생성
	public User toEntity() {
		return User.builder()
			.name(name)
			.email(email)
			.password("0000")
			.loginType(loginType)
			.membershipLevel(User.MembershipLevel.FREE)
			.role(User.Role.USER)
			.build();
	}

}
