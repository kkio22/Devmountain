package nbc.devmountain.domain.chat.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.user.model.User;

@RequiredArgsConstructor
@Component
@Slf4j
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
		HttpSession httpSession = servletRequest.getSession(false);

		if (httpSession != null) {
			SessionUser sessionUser = (SessionUser)httpSession.getAttribute("user");
			if (sessionUser != null) {
				attributes.put("user", sessionUser);
				attributes.put("isLoggedIn", true);
				attributes.put("membershipType",sessionUser.getMembershipLevel());
				log.info("웹소켓 연결 - 로그인 사용자: {} / 멤버십: {}", sessionUser.getUserId(), sessionUser.getMembershipLevel());

			} else {
				// 비로그인 사용자 (세션은 있지만 로그인 정보 없음)
				attributes.put("user", null);
				attributes.put("isLoggedIn", false);
				attributes.put("membershipType", User.MembershipLevel.GUEST); // 명시적으로 GUEST!
				log.info("웹소켓 연결 - 비회원 사용자 (세션 없음)");
			}
		} else {
			// 세션 없는 비회원 사용자
			attributes.put("user", null);
			attributes.put("isLoggedIn", false);
			attributes.put("membershipType", User.MembershipLevel.GUEST); // 명시적으로 GUEST!
			log.info("웹소켓 연결 - 비회원 사용자 (세션 없음)");
		}

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {
	}
}