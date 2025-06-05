package nbc.devmountain.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpSession;
import nbc.devmountain.domain.user.model.User;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {

		HttpSession session = ((ServletServerHttpRequest)request).getServletRequest().getSession(false);

		if (session != null) { //로그인 후 세션이 있음
			User user = (User)session.getAttribute("LOGIN_USER");

			if (user != null) {
				attributes.put("user", user);
				attributes.put("userId", user.getUserId());
				String userType = getUserType(user.getMembershipLevel());
				attributes.put("userType", userType);
			} else {
				// 비로그인 유저
				setGuestAttributes(attributes);
			}
		} else {
			//세션이 없는 비로그인 유저인경우
			setGuestAttributes(attributes);
		}

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {
	}

	private String getUserType(User.MembershipLevel membershipLevel) {
		if (membershipLevel == null) {
			return "GUEST";
		}
		return switch (membershipLevel) {
			case FREE -> "FREE";
			case PRO -> "PRO";
		};
	}

	private void setGuestAttributes(Map<String, Object> attributes) {
		attributes.put("userType", "GUEST");
		attributes.put("chatCount", 0);
		attributes.put("user", null);
		attributes.put("userId", null);
	}
}