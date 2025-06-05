package nbc.devmountain.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nbc.devmountain.domain.user.model.User;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {
	//사용자 인증검증을 위한 인터셉터
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {
		HttpSession session = ((ServletServerHttpRequest)request).getServletRequest().getSession(false);

		if(session !=null){
			User user = (User)session.getAttribute("");//세션-스프링컨텍스트명
			attributes.put("user",user); //
		}

		return true;

	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
