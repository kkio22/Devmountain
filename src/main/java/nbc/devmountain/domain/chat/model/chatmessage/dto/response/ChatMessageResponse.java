package nbc.devmountain.domain.chat.model.chatmessage.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatMessage;

@Getter
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

	private Long chatroomId;
	private Long chatId;
	private Long userId;
	private String message;
	private Boolean isAiResponse;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

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
