package nbc.devmountain.common.util.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import nbc.devmountain.domain.user.model.User;

public class CustomUserPrincipal implements UserDetails, OAuth2User {

	private final User user; // 일반 로그인
	private Map<String, Object> attributes; // oauth2 로그인

	// 일반 로그인용
	public CustomUserPrincipal(User user) {
		this.user = user;
	}

	// OAuth2 로그인용
	public CustomUserPrincipal(User user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}

	// 사용자 권한 정보
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (user.getRole() == null) {
			throw new IllegalStateException("User role is null for user: " + user.getEmail());
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}



	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	public Long getUserId() {
		return user.getUserId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// OAuth2User 필수 메서드
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return String.valueOf(user.getUserId());
	}
}

