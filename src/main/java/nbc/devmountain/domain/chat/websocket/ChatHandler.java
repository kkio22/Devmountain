package nbc.devmountain.domain.chat.websocket;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.chatmessage.service.ChatMessageService;
import nbc.devmountain.common.ai.AIResponseService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandler extends TextWebSocketHandler {

	private final AIResponseService aiResponseService;
	private final ChatMessageService chatMessageService;
	private final WebSocketMessageSender messageSender;
	private final WebSocketSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	@Override  //웹소켓 연결시 호출되는 메서드(사용자검증)
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		SessionUser sessionUser = (SessionUser)session.getAttributes().get("user");
		Boolean isLoggedIn = (Boolean)session.getAttributes().get("isLoggedIn");
		Long roomId = getRoomId(session);

		//세션등록
		sessionManager.addSession(roomId, session);

		//로그인유저
		if (isLoggedIn) {
			log.info("웹소켓 연결 성공 - sessionId: {}, userId: {}, roomId: {}",
				session.getId(), sessionUser.getUserId(), roomId);

			//이전 메세지 보내기
			List<ChatMessageResponse> history = chatMessageService.getMessages(sessionUser.getUserId(), roomId);
			for (ChatMessageResponse msgDto : history) {
				messageSender.sendMessageToRoom(roomId, msgDto);
			}
			//비회원
		} else {
			log.info("웹소켓 연결 성공 - sessionId: {}, 비회원 사용자, roomId: {}", session.getId(), roomId);

			ChatMessageResponse welcomeMsg = ChatMessageResponse.builder()
				.message("안녕하세요. 비회원으로 접속하셨습니다.")
				.isAiResponse(true)
				.build();
			messageSender.sendMessageToRoom(roomId, welcomeMsg);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		SessionUser sessionUser = (SessionUser)session.getAttributes().get("user");
		Boolean isLoggedIn = (Boolean)session.getAttributes().get("isLoggedIn");
		Long roomId = getRoomId(session);
		String payload = message.getPayload();

		if (isLoggedIn) {
			//사용자 메세지저장
			ChatMessageResponse userMsg =
				chatMessageService.createMessage(sessionUser.getUserId(), roomId, payload);
			//사용자 메세지 전송
			messageSender.sendMessageToRoom(roomId, userMsg);

			//AI응답 저장
			ChatMessageResponse aiResponse =
				aiResponseService.processChat(sessionUser, roomId, payload);
			//AI응답 전송
			messageSender.sendMessageToRoom(roomId, aiResponse);
		} else {
			//비회원유저 메세지제한
			if (exceededGuestLimit(session)) {
				ChatMessageResponse limitMsg = ChatMessageResponse.builder()
					.message("비회원은 최대 5개의 메시지만 보낼 수 있습니다.")
					.isAiResponse(true)
					.build();
				messageSender.sendMessageToRoom(roomId, limitMsg);
				return;
			}
			//DB 저장 없이 메세지반환
			ChatMessageResponse userMsg = ChatMessageResponse.builder()
				.message(payload)
				.isAiResponse(false)
				.build();
			messageSender.sendMessageToRoom(roomId, userMsg);

			ChatMessageResponse aiResponse =
				aiResponseService.processGuestChat(roomId, payload);
			messageSender.sendMessageToRoom(roomId, aiResponse);
		}

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

	private boolean exceededGuestLimit(WebSocketSession session) {
		Map<String, Object> attrs = session.getAttributes();
		Integer count = (Integer)attrs.getOrDefault("guestCount", 0);

		if (count >= 5)
			return true;
		attrs.put("guestCount", count + 1);
		return false;
	}
}
