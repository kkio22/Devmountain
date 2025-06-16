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

		log.debug("[ChatMessageResponse] 변환 시작 - AI응답: {}, 메시지타입: {}, 메시지: {}",
			chatMessage.getIsAiResponse(), chatMessage.getMessageType(),
			chatMessage.getMessage() != null ? chatMessage.getMessage().substring(0, Math.min(100, chatMessage.getMessage().length())) + "..." : "null");

		// AI 응답인 경우 (isAiResponse가 true)
		if (chatMessage.getIsAiResponse()) {
			if (chatMessage.getMessageType() == MessageType.RECOMMENDATION) {
				String rawMessage = chatMessage.getMessage();
				log.info("[ChatMessageResponse] 추천 메시지 파싱 시작, 원본 길이: {}",
					rawMessage != null ? rawMessage.length() : 0);

				if (rawMessage == null || rawMessage.trim().isEmpty()) {
					log.warn("[ChatMessageResponse] 추천 메시지가 비어있음");
					builder.message("추천 데이터가 비어있습니다.")
						.recommendations(Collections.emptyList())
						.messageType(MessageType.ERROR);
				} else {
					try {
						log.debug("[ChatMessageResponse] JSON 파싱 시도: {}",
							rawMessage.substring(0, Math.min(200, rawMessage.length())));

						List<RecommendationDto> recommendationsList = objectMapper.readValue(
							rawMessage,
							new TypeReference<List<RecommendationDto>>() {}
						);

						log.info("[ChatMessageResponse] 추천 파싱 성공: {} 개 항목",
							recommendationsList != null ? recommendationsList.size() : 0);

						if (recommendationsList != null && !recommendationsList.isEmpty()) {
							for (int i = 0; i < recommendationsList.size(); i++) {
								RecommendationDto rec = recommendationsList.get(i);
								log.debug("[ChatMessageResponse] 추천 {}: lectureId={}, title={}",
									i + 1, rec.lectureId(), rec.title());
							}
						}

						builder.recommendations(recommendationsList != null ? recommendationsList : Collections.emptyList())
							.message(null);
					} catch (JsonProcessingException e) {
						log.error("[ChatMessageResponse] 추천 메시지 파싱 실패: {}", e.getMessage());
						log.error("[ChatMessageResponse] 파싱 실패한 원본 데이터: {}", rawMessage);
						builder.message("추천 응답을 처리하는 중 오류가 발생했습니다.")
							.recommendations(Collections.emptyList())
							.messageType(MessageType.ERROR);
					}
				}
			} else {
				// 일반 AI 메시지
				log.debug("[ChatMessageResponse] 일반 AI 메시지 처리");
				builder.message(chatMessage.getMessage())
					.recommendations(Collections.emptyList());
			}
		} else {
			// 사용자 메시지
			log.debug("[ChatMessageResponse] 사용자 메시지 처리");
			builder.message(chatMessage.getMessage())
				.recommendations(Collections.emptyList());
		}

		ChatMessageResponse result = builder.build();
		log.debug("[ChatMessageResponse] 변환 완료 - 추천 개수: {}, 메시지 존재: {}",
			result.recommendations != null ? result.recommendations.size() : 0,
			result.message != null);

		return result;
	}
}