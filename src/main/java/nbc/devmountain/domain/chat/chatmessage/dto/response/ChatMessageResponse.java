// src/main/java/nbc/devmountain/domain/chat/chatmessage/dto/response/ChatMessageResponse.java
package nbc.devmountain.domain.chat.chatmessage.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.model.ChatMessage;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResponse {
	private final Long chatroomId;
	private final Long chatId;
	private final Long userId;
	private final String message;
	private final List<RecommendationDto> recommendations;
	private final boolean isAiResponse;
	private final MessageType messageType;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static ChatMessageResponse from(ChatMessage chatMessage) {
		Long userId = (chatMessage.getUser() != null) ? chatMessage.getUser().getUserId() : null;
		ChatMessageResponseBuilder builder = ChatMessageResponse.builder()
			.chatroomId(chatMessage.getChatRoom().getChatroomId())
			.chatId(chatMessage.getChatId())
			.userId(userId)
			.isAiResponse(chatMessage.getIsAiResponse())
			.createdAt(chatMessage.getCreatedAt())
			.updatedAt(chatMessage.getUpdatedAt());

		if (chatMessage.getIsAiResponse()) {
			try {
				List<RecommendationDto> recommendationsList = objectMapper.readValue(
					chatMessage.getMessage(),
					new TypeReference<List<RecommendationDto>>() {
					}
				);
				builder.recommendations(recommendationsList)
					.message(null)
					.messageType(MessageType.RECOMMENDATION);
			} catch (JsonProcessingException e) {
				builder.message("응답을 처리하는 중 오류가 발생했습니다.")
					.recommendations(Collections.emptyList())
					.messageType(MessageType.ERROR);
			}
		} else {
			builder.message(chatMessage.getMessage())
				.recommendations(Collections.emptyList())
				.messageType(MessageType.CHAT);
		}

		return builder.build();
	}

	public enum MessageType {
		CHAT,           // 일반 채팅
		RECOMMENDATION, // 강의 추천
		ERROR,          // 에러 메시지
		WELCOME         // 환영 메시지
	}
}