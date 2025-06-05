package nbc.devmountain.domain.chatroom.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chatmessage.dto.response.ChatMessageResponse;

@Builder
@Getter
public class ChatRoomDetailResponse {

	private Long chatroomId;
	private String chatroomName;
	private LocalDateTime createdAt;
	private String roomType;
	private List<ChatMessageResponse> messages;

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
