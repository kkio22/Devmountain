package nbc.devmountain.domain.chat.websocket;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.dto.ChatRoomNameUpdateResponse;
import nbc.devmountain.domain.chat.model.MessageType;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketMessageSender {
	private final ObjectMapper objectMapper;
	private final WebSocketSessionManager sessionManager;

	public void sendMessage(WebSocketSession session, ChatMessageResponse message) {
		try {
			if (session.isOpen()) {
				String jsonMessage = objectMapper.writeValueAsString(message);
				session.sendMessage(new TextMessage(jsonMessage));
				log.debug("메세지 전송 완료 : sessionId={}", session.getId());
			} else {
				log.warn("메세지 전송 실패 : sessionId={}", session.getId());
			}
		} catch (Exception e) {
			log.error("메세지 전송 중 오류 발생 : sessionId={}", session.getId(), e);
			throw new RuntimeException("메세지 전송 실패");
		}
	}

	public void sendMessageToRoom(Long roomId, ChatMessageResponse message) {
		WebSocketSession session = sessionManager.getSession(roomId);
		if (session != null) {
			sendMessage(session, message);
		}
	}

	public void sendRoomNameUpdate(Long roomId, String newName) {
		WebSocketSession session = sessionManager.getSession(roomId);
		try {
			if (session != null) {
				ChatRoomNameUpdateResponse msg = new ChatRoomNameUpdateResponse(roomId, newName);

				String json = objectMapper.writeValueAsString(msg);
				session.sendMessage(new TextMessage(json));
				log.info("메세지 전송 완료 : sessionId={}", session.getId());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
