package nbc.devmountain.domain.chatroom.dto.response;

import java.time.LocalDateTime;


public record ChatRoomResponse(Long chatroomId,
							   String chatroomName,
							   LocalDateTime createdAt,
							   String roomType) {

}