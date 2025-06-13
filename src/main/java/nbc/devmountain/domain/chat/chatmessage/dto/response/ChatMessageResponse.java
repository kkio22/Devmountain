package nbc.devmountain.domain.chat.chatmessage.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.model.ChatMessage;

@Getter
@Builder
public class ChatMessageResponse {

	private final Long chatroomId;
	private final Long chatId;
	private final Long userId;
	private final String message;
	private final List<RecommendationDto> recommendations; // AI 추천 목록을 담을 새로운 필드
	private final boolean isAiResponse;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	// JSON 파싱을 위한 ObjectMapper
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * [수정] ChatMessage 엔티티를 이 DTO로 변환하는 핵심 로직입니다.
	 * AI 응답인지 아닌지에 따라 message 또는 recommendations 필드에 값을 채웁니다.
	 */
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
			// AI 응답일 경우, DB에 저장된 message 문자열을 List<RecommendationDto>로 파싱합니다.
			try {
				List<RecommendationDto> recommendationsList = objectMapper.readValue(
					chatMessage.getMessage(),
					new TypeReference<List<RecommendationDto>>() {}
				);
				builder.recommendations(recommendationsList).message(null); // recommendations 필드를 채웁니다.
			} catch (JsonProcessingException e) {
				// 파싱에 실패하면 클라이언트에게 보여줄 에러 메시지를 생성합니다.
				RecommendationDto errorDto = new RecommendationDto("응답을 처리하는 중 오류가 발생했습니다.", "", "", "");
				builder.recommendations(Collections.singletonList(errorDto)).message(null);
			}
		} else {
			// 일반 사용자 메시지일 경우, message 필드만 채웁니다.
			builder.message(chatMessage.getMessage()).recommendations(Collections.emptyList());
		}

		return builder.build();
	}
}
