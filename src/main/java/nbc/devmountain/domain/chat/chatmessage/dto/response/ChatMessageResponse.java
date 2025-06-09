package nbc.devmountain.domain.chat.chatmessage.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.devmountain.domain.chat.model.ChatMessage;

@Builder
public record ChatMessageResponse(Long chatroomId,
								  Long chatId,
								  Long userId,
								  String message,
								  Boolean isAiResponse,
								  LocalDateTime createdAt,
								  LocalDateTime updatedAt) {

	public static ChatMessageResponse from(ChatMessage chatMessage) {
		return ChatMessageResponse.builder()
			.chatId(chatMessage.getChatId())
			.chatroomId(chatMessage.getChatRoom().getChatroomId())
			.userId(chatMessage.getUser().getUserId())
			.message(chatMessage.getMessage())
			.isAiResponse(chatMessage.getIsAiResponse())
			.createdAt(chatMessage.getCreatedAt())
			.updatedAt(chatMessage.getUpdatedAt())
			.build();
	}
}
