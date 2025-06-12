package nbc.devmountain.domain.ai.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.util.security.SessionUser;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.chatmessage.service.ChatMessageService;
import nbc.devmountain.domain.chat.websocket.WebSocketMessageSender;
import nbc.devmountain.domain.user.model.User;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

	private final ChatMessageService chatMessageService;
	private final LectureRecommendationService recommendationService;
	private final WebSocketMessageSender messageSender;
	private final ObjectMapper objectMapper;

	public void handleMessage(WebSocketSession session, Long roomId, String payload) {
		SessionUser sessionUser = (SessionUser)session.getAttributes().get("user");
		User.MembershipLevel membershipType = (User.MembershipLevel)session.getAttributes().get("membershipType");

		//비회원 유저 : 메세지 제한두기
		if (membershipType == User.MembershipLevel.GUEST) {
			Integer geustCount = (Integer)session.getAttributes().getOrDefault("geustCount", 0);
			if (geustCount >= 5) {
				ChatMessageResponse limitMsg = ChatMessageResponse.builder()
					.message("비회원은 최대 5개의 메세지만 보낼 수 있습니다."
						+ "더 이용하려면 로그인을 해주세요.")
					.isAiResponse(true)
					.build();
				messageSender.sendMessageToRoom(roomId, limitMsg);
				return;
			}
			//메세지 전
			session.getAttributes().put("geustCount", geustCount + 1);
		}

		//메세지 저장 /비회원 저장x
		ChatMessageResponse userMsg;
		if (membershipType != User.MembershipLevel.GUEST) {
			userMsg = chatMessageService.createMessage(sessionUser.getUserId(), roomId, payload);
		} else {
			userMsg = ChatMessageResponse.builder()
				.message(payload)
				.recommendations(Collections.emptyList())
				.isAiResponse(false)
				.build();
		}
		messageSender.sendMessageToRoom(roomId, userMsg);

		//AI 추천 응답
		AiRecommendationResponse aiResponse =
			recommendationService.recommendationResponse(payload, membershipType);

		//AI 메세지 저장
		ChatMessageResponse aiMsg;
		if (membershipType != User.MembershipLevel.GUEST) {
			aiMsg = chatMessageService.createAIMessage(roomId, aiResponse);
			log.info("회원 AI 메세지 생성 완료");
		} else {
			try {
				String recJson = objectMapper.writeValueAsString(aiResponse.recommendations());
				aiMsg = ChatMessageResponse.builder()
					.message(recJson)
					.recommendations(aiResponse.recommendations())
					.isAiResponse(true)
					.build();
				log.info("비회원 AI 메세지 생성 완료");
			} catch (JsonProcessingException e) {
				log.error("비회원 AI 응답 메세지 직렬화 실패", e);
				throw new RuntimeException(e);
			}
		}
		messageSender.sendMessageToRoom(roomId, aiMsg);
	}

	public List<ChatMessageResponse> getChatHistory(Long userId, Long roomId) {
		// ChatMessageService를 이용해 해당 유저의 채팅방 메시지 목록 조회
		return chatMessageService.getMessages(userId, roomId);
	}

	public ChatMessageResponse getGuestWelcomeMsg() {
		return ChatMessageResponse.builder()
			.message("안녕하세요. 비회원으로 접속하셨습니다.")
			.isAiResponse(true)
			.build();
	}

}
