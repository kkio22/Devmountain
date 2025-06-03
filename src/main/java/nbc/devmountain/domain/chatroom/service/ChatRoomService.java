package nbc.devmountain.domain.chatroom.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.model.RoomType;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomDetailResponse;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	public ChatRoomResponse createChatRoom(Long userId, String chatroomName) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		ChatRoom chatRoom = chatRoomRepository.save(
			ChatRoom.builder()
				.user(user)
				.chatroomName(chatroomName)
				.type(RoomType.FREE)
				.build());

		return ChatRoomResponse.from(chatRoom);
	}

	public List<ChatRoomResponse> findAllChatRooms(long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		List<ChatRoom> chatRoomList = chatRoomRepository.findAllByUser(user);

		return chatRoomList.stream()
			.map(ChatRoomResponse::from)
			.toList();
	}

	public ChatRoomDetailResponse findChatRoom(long userId, Long chatroomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		return ChatRoomDetailResponse.from(chatRoom);
	}
}
