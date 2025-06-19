package nbc.devmountain.domain.chat.sse.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.websocket.WebSocketSessionManager;

@Component
@RequiredArgsConstructor
public class TypingWebSocketHandler extends TextWebSocketHandler{

	private final ObjectMapper objectMapper;
	private final WebSocketSessionManager sessionManager;

	public void sendTypingEffect(String fullMessage,Long roomId){
		CompletableFuture.runAsync(() -> {
			try{
				WebSocketSession session = sessionManager.getSession(roomId);
				if(session==null||!session.isOpen()) return;

				String[] characters = fullMessage.split("");

				for(int i = 0; i < characters.length; i++){
					Typing
				}
			}
		})
	}
}
