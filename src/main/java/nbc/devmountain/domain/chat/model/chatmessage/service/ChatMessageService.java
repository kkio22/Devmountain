package nbc.devmountain.domain.chat.model.chatmessage.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.repository.ChatMessageRepository;
import nbc.devmountain.domain.chat.model.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	public ChatMessageResponse createMessage(Long userId, Long chatRoomId, String message) {
		// //방이 생성되 있을 경우
		// User user = userRepository.findById(userId)
		// 	.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		//
		// ChatRoom chatRoom;
		//
		// if (chatRoomId != null) {
		// 	chatRoom = chatRoomRepository.findById(chatRoomId)
		// 		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		// } else if () {
		//
		// }
		return null;
	}
}

