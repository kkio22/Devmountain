package nbc.devmountain.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import nbc.devmountain.domain.chat.model.ChatRoom;

@Builder
public record ChatRoomDetailResponse(Long chatroomId,
									 String chatroomName,
									 LocalDateTime createdAt,
									 String roomType,
									 List<ChatMessageResponse> messages) {

	public static ChatRoomDetailResponse from(ChatRoom chatRoom) {
		return ChatRoomDetailResponse.builder()
			.chatroomId(chatRoom.getChatroomId())
			.chatroomName(chatRoom.getChatroomName())
			.createdAt(chatRoom.getCreatedAt())
			.roomType(chatRoom.getType().toString())
			.messages(
				chatRoom.getMessages()
					.stream()
					.map(ChatMessageResponse::from)
					.collect(Collectors.toList())
			)
			.build();
	}
}
