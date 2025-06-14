package nbc.devmountain.domain.chat.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.devmountain.domain.chat.model.ChatRoom;

@Builder
public record ChatRoomResponse(Long chatroomId,
							   String chatroomName,
							   String roomType,
							   LocalDateTime createdAt) {


	public static ChatRoomResponse from(ChatRoom chatRoom) {
		return ChatRoomResponse.builder()
			.chatroomId(chatRoom.getChatroomId())
			.chatroomName(chatRoom.getChatroomName())
			.createdAt(chatRoom.getCreatedAt())
			.roomType(chatRoom.getType().toString())
			.build();
	}
}