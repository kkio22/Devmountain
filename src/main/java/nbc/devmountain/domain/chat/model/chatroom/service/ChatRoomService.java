package nbc.devmountain.domain.chat.model.chatroom.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.model.RoomType;
import nbc.devmountain.domain.chat.model.chatroom.dto.response.ChatRoomDetailResponse;
import nbc.devmountain.domain.chat.model.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chat.model.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	@Transactional
	public ChatRoomResponse createChatRoom(User user, String chatroomName) {
		ChatRoom chatRoom = ChatRoom.builder()
			.user(user)
			.chatroomName(chatroomName)
			.type(RoomType.valueOf(user.getMembershipLevel().name()))
			.build();

		return ChatRoomResponse.from(chatRoomRepository.save(chatRoom));
	}

	@Transactional(readOnly = true)
	public List<ChatRoomResponse> findAllChatRooms(User user) {

		List<ChatRoom> chatRooms = chatRoomRepository.findAllByUser(user);

		return chatRooms.stream()
			.map(ChatRoomResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public ChatRoomDetailResponse findChatRoom(User user, Long chatroomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(user.getUserId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		return ChatRoomDetailResponse.from(chatRoom);
	}

	@Transactional
	public ChatRoomResponse updateChatRoomName(User user, Long chatroomId, String newName) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(user.getUserId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		chatRoom.updateName(newName);

		return ChatRoomResponse.from(chatRoom);
	}

	@Transactional
	public void deleteChatRoom(User user, Long chatroomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(user.getUserId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		chatRoom.delete();
	}
}
