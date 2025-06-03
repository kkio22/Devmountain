package nbc.devmountain.domain.chat.model.chatmessage.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatMessage;

@Getter
@RequiredArgsConstructor
@Builder
public class ChatMessageResponse {

	private final Long chatId;
	private final String sender;
	private final String message;
	private final LocalDateTime createdAt;

	public static ChatMessageResponse from(ChatMessage chatMessage) {
		return ChatMessageResponse.builder()
			.chatId(chatMessage.getChatId())
			.message(chatMessage.getMessage())
			.createdAt(chatMessage.getCreatedAt())
			.build();
	}
}
