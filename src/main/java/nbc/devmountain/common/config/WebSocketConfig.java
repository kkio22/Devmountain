package nbc.devmountain.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.websocket.ChatHandler;
import nbc.devmountain.domain.chat.websocket.HttpHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatHandler chatHandler;
	private final HttpHandshakeInterceptor handshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatHandler, "/ws/chat")
			.addInterceptors(handshakeInterceptor)
			.setAllowedOrigins("*");
	}
}
