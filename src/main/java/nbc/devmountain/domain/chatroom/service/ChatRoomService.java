package nbc.devmountain.domain.chatroom.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chatroom.dto.request.CreateChatRoomRequest;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	public ChatRoomResponse createChatRoom(Long userId,String chatroomName) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		chatRoomRepository.save(
			ChatRoom.builder()
				.user(user)
				.chatroomName(chatroomName)
				.build()
		);

		return null;
	}

}
