package nbc.devmountain.domain.chat.model.chatmessage.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatMessagRequest {

	private final String message;
}
