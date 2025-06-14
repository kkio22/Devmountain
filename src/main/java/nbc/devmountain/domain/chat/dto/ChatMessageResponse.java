package nbc.devmountain.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.MessageType;
@Slf4j
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
			.updatedAt(chatMessage.getUpdatedAt())
			.messageType(chatMessage.getMessageType());

		// AI 응답인 경우 (isAiResponse가 true)
		if (chatMessage.getIsAiResponse()) {
			if (chatMessage.getMessageType() == MessageType.RECOMMENDATION) {
				try {
					List<RecommendationDto> recommendationsList = objectMapper.readValue(
						chatMessage.getMessage(),
						new TypeReference<List<RecommendationDto>>() {
						}
					);
					builder.recommendations(recommendationsList).message(null);
				} catch (JsonProcessingException e) {
					builder.message("추천 응답을 처리하는 중 오류가 발생했습니다.")
						.recommendations(Collections.emptyList())
						.messageType(MessageType.ERROR);
					log.error("[ChatMessageResponse] 추천 메시지 파싱 실패: {}", e.getMessage());
				}
			} else {
				builder.message(chatMessage.getMessage()).recommendations(Collections.emptyList());
			}
		} else {
			builder.message(chatMessage.getMessage()).recommendations(Collections.emptyList());
		}
		return builder.build();
	}
}