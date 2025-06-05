package nbc.devmountain.websocket;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.service.ChatMessageService;
import nbc.devmountain.domain.user.model.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandler extends TextWebSocketHandler {
	private final ChatMessageService chatMessageService;
	private final ObjectMapper objectMapper;

	//ConcurrentHashMap-여러 스레드에서 접근하는걸 막아줌(Lock) 사용. 동시성문제해결

	// roomId   : WebSocketSession
	private final Map<Long, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
	//sessionId : roomId
	private final Map<String, Long> sessionToRoom = new ConcurrentHashMap<>();

	/**웹소켓이 연결되면 호출되는 메서드*/
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Long roomId = getRoomId(session);
		User user = (User)session.getAttributes().get("user");

		//세션 등록
		activeSessions.put(roomId, session);
		sessionToRoom.put(session.getId(), roomId);

		log.info("WebSocket 연결");

	}

	/**메세지 발송시 호출되는 메서드*/
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		User user = (User)session.getAttributes().get("user");
		String payload = message.getPayload(); //사용자가 보낸 텍스트
		Long roomId = sessionToRoom.get(session.getId());

		ChatMessageResponse userMessage;
		//사용자 메세지
		try {
			if (user != null) { //유저가 보낸 메세지 저장
				userMessage = chatMessageService.createMessage(user.getUserId(), roomId, payload);
			} else { //비로그인 유저면 임시생성
				userMessage = ChatMessageResponse.builder()
					.chatId(System.currentTimeMillis())
					.chatroomId(roomId)
					.userId(null)
					.message(payload)
					.isAiResponse(false)
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.build();
			}
			//사용자 메세지 전송
			if (session.isOpen()) {
				String jsonMessage = objectMapper.writeValueAsString(userMessage);
				session.sendMessage(new TextMessage(jsonMessage));
			}
			//AI 응답 요청
			//AI 응답 데이터베이스 저장하기
			// chatMessageService.createAIMessage(roomId, AI 응답)

		}catch (Exception e){
			log.error("메세지 처리 실패",e);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String sessionId = session.getId();
		Long roomId = sessionToRoom.remove(sessionId);
		if(roomId != null){
			activeSessions.remove(roomId);
		}
		log.info("WebSocket 연결 종료");
	}

	private Long getRoomId(WebSocketSession session) {
		//ws://localhost:8080/ws/chat?roomId= 에서 roomId= 만 가져옴
		URI uri = session.getUri(); //c
		String query = uri.getQuery();

		if (query == null || !query.contains("roomId=")) {
			throw new IllegalArgumentException("roomId parameter required");
		}
		String[] params = query.split("&");
		for (String param : params) {
			if (param.startsWith("roomId=")) {
				try {
					return Long.parseLong(param.split("=")[1]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid roomId format");
				}
			}
		}
		throw new IllegalArgumentException("roomId parameter not found");
	}

}