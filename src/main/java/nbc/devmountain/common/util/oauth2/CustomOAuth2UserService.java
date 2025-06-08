package nbc.devmountain.common.util.oauth2;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final UserRepository userRepository;
	private final HttpServletRequest request;
	private final PasswordEncoder passwordEncoder;


	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// provider 추(google 등)
		// String provider = userRequest.getClientRegistration().getClientId();

		// OAuth2로 사용자 정보 load
		OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

		// email 정보 저장
		String email = oAuth2User.getAttribute("email");

		if (email == null) {
			throw new OAuth2AuthenticationException("이메일은 필수입니다.");
		}

		// OAuth2 등록 ID 추출
		String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, naver
		// id 추출
		String userNameAttribute = userRequest.getClientRegistration()
			.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		// OAuth2로부터 받은 정보를 OAuthAttributes로 매핑
		OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttribute, oAuth2User.getAttributes());

		// 유저가 이미 존재하는지 확인하거나 새로 생성
		User user = userRepository.findByEmail(email).orElseGet(() -> {
			// 처음 로그인한 경우 사용자 생성
			return userRepository.save(
				User.builder()
					.email(email)
					.password(passwordEncoder.encode("0000")) // 임시 비번
					.name(attributes.getName())
					.phoneNumber("010-0000-0000")
					.loginType(User.LoginType.GOOGLE) // 추후 kakao 등 추가 예정
					.role(User.Role.USER) // 기본 admin설정
					.membershipLevel(User.MembershipLevel.FREE) // 기본 membership 설정
					.build()
			);
		});

		// 세션에 유저 정보 저장
		request.getSession().setAttribute("user", new SessionUser(user));

		return new CustomUserPrincipal(user, attributes.getAttributes());
	}

	// 유저 정보를 저장하거나 이미 존재할 경우 기존 사용자 정보 반환
	private User saveOrUpdate(OAuthAttributes attributes) {
		return userRepository.findByEmail(attributes.getEmail())
			.orElseGet(() -> userRepository.save(attributes.toEntity()));
	}
}
