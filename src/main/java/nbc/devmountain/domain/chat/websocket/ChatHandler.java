package nbc.devmountain.domain.chat.websocket;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.ai.service.ChatService;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandler extends TextWebSocketHandler {

	private final ChatService chatService;
	private final WebSocketSessionManager sessionManager;
	private final WebSocketMessageSender messageSender;

	@Override  //웹소켓 연결시 호출되는 메서드(사용자검증)
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		SessionUser sessionUser = (SessionUser)session.getAttributes().get("user");
		boolean isLoggedIn = (sessionUser != null);
		Long roomId = getRoomId(session);

		// 새 WebSocket 세션을 세션 매니저에 등록 (채팅방 참가)
		sessionManager.addSession(roomId, session);

		if (isLoggedIn) {
			log.info("웹소켓 연결 성공 - sessionId: {}, userId: {}, roomId: {}",
				session.getId(), sessionUser.getUserId(), roomId);
			// 이전 채팅 내역 불러오기
			List<ChatMessageResponse> history = chatService.getChatHistory(sessionUser.getUserId(), roomId);
			// 이전 메시지 채팅방에 전송
			for (ChatMessageResponse oldMsg : history) {
				messageSender.sendMessageToRoom(roomId, oldMsg);
			}
		} else {
			log.info("웹소켓 연결 성공 - sessionId: {}, 비회원 사용자, roomId: {}", session.getId(), roomId);
			// 비회원 환영 메시지 생성 및 전송 (대화 저장 없음, 단순 안내용)
			ChatMessageResponse welcomeMsg = chatService.getGuestWelcomeMsg();
			messageSender.sendMessageToRoom(roomId, welcomeMsg);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		Long roomId = getRoomId(session);
		chatService.handleMessage(session, roomId, payload);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("웹소켓 연결 해제 - sessionId: {}", session.getId());
		sessionManager.removeSession(session);
	}

	private Long getRoomId(WebSocketSession session) {
		String query = session.getUri().getQuery();
		if (query == null || !query.contains("=")) {
			throw new IllegalArgumentException("roomId 파라미터가 없습니다.");
		}
		String[] params = query.split("=");
		return Long.parseLong(params[1]);
	}
}
