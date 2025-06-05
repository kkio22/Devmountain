package nbc.devmountain.domain.chat.model.chatroom.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.chat.model.ChatRoom;

@Builder
@Getter
public class ChatRoomResponse {
	private Long chatroomId;
	private String chatroomName;
	private String roomType;
	private LocalDateTime createdAt;

	public static ChatRoomResponse from(ChatRoom chatRoom) {
		return ChatRoomResponse.builder()
			.chatroomId(chatRoom.getChatroomId())
			.chatroomName(chatRoom.getChatroomName())
			.createdAt(chatRoom.getCreatedAt())
			.roomType(chatRoom.getType().toString())
			.build();
	}
}