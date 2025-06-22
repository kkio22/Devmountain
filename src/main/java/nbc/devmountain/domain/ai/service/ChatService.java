package nbc.devmountain.domain.ai.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;

import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.chat.service.ChatMessageService;
import nbc.devmountain.domain.chat.websocket.WebSocketMessageSender;
import nbc.devmountain.domain.user.model.User;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {
	private final ChatMessageService chatMessageService;
	private final LectureRecommendationService recommendationService;
	private final WebSocketMessageSender messageSender;

	public ChatMessageResponse handleMessage(WebSocketSession session, Long roomId, String payload) {
		SessionUser sessionUser = (SessionUser)session.getAttributes().get("user");
		User.MembershipLevel membershipType = (User.MembershipLevel)session.getAttributes().get("membershipType");

		if (membershipType == User.MembershipLevel.GUEST) {
			Integer guestCount = (Integer)session.getAttributes().getOrDefault("guestCount", 0);
			if (guestCount >= 5) {
				ChatMessageResponse limitMsg = ChatMessageResponse.builder()
					.message("비회원은 최대 5개의 메세지만 보낼 수 있습니다. 더 이용하려면 로그인을 해주세요.")
					.isAiResponse(true)
					.messageType(MessageType.ERROR)
					.build();
				messageSender.sendMessageToRoom(roomId, limitMsg);
				return limitMsg;
			}
			session.getAttributes().put("guestCount", guestCount + 1);
		}

		ChatMessageResponse userMsg;
		if (membershipType != User.MembershipLevel.GUEST) {
			userMsg = chatMessageService.createMessage(sessionUser.getUserId(), roomId, payload);
			log.info("회원 메세지 생성 완료");
		} else {
			userMsg = ChatMessageResponse.builder()
				.message(payload)
				.recommendations(Collections.emptyList())
				.isAiResponse(false)
				.messageType(MessageType.CHAT)
				.build();
		}
		messageSender.sendMessageToRoom(roomId, userMsg);


		ChatMessageResponse aiResponse = recommendationService.recommendationResponse(payload, membershipType, roomId);
		ChatMessageResponse aiMsg;
		if (membershipType != User.MembershipLevel.GUEST) {
			aiMsg = chatMessageService.createAIMessage(roomId, aiResponse);
			log.info("AI 메세지 생성 완료");
		} else {
			aiMsg = aiResponse;
		}
		messageSender.sendMessageToRoom(roomId, aiMsg);
		return aiMsg;
	}

	public List<ChatMessageResponse> getChatHistory(Long userId, Long roomId) {
		return chatMessageService.getMessages(userId, roomId);
	}

	public ChatMessageResponse getGuestWelcomeMsg() {
		return ChatMessageResponse.builder()
			.message("안녕하세요. 비회원으로 접속하셨습니다.")
			.isAiResponse(true)
			.messageType(MessageType.WELCOME)
			.build();
	}
}
