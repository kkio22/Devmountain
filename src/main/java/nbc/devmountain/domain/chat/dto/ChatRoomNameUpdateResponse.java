package nbc.devmountain.domain.chat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ChatRoomNameUpdateResponse {
	private final String type = "ROOM_NAME_UPDATE";
	private final Long roomId;
	private final String roomName;
}


