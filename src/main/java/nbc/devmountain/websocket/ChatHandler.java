package nbc.devmountain.websocket;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandler extends TextWebSocketHandler {
	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("웹소켓 연결됨 - sessionId: {}", session.getId());

		// 연결 시 메시지 전송
		sendMessage(session, "안녕하세요!");
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String userMessage = message.getPayload();
		log.info("유저 메시지 : {}", userMessage);

		String response = "받은 메시지 : " + userMessage + " ";
		sendMessage(session, response);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("웹소켓 연결 해제 - sessionId: {}", session.getId());
	}

	private void sendMessage(WebSocketSession session, String message) {
		try {
			//세션상태 open
			if (session.isOpen()) {
				// JSON 형태로 응답 (ChatMessageResponse 형태 유지)
				ChatMessageResponse response = ChatMessageResponse.builder()
					.chatId(System.currentTimeMillis())
					.chatroomId(1L) // 임시 roomId
					.userId(null)
					.message(message)
					.isAiResponse(true)
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.build();

				String jsonMessage = objectMapper.writeValueAsString(response);
				session.sendMessage(new TextMessage(jsonMessage));
				log.info("메시지 전송 완료: {}", message);
			}
		} catch (Exception e) {
			log.error("메시지 전송 실패", e);
		}
	}
}