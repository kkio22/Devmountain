package nbc.devmountain.domain.chatroom.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateChatRoomRequest {

	private final String chatroomName;

	// private final String room_type;
}
