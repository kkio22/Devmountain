package nbc.devmountain.domain.chat.model.chatmessage.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.model.RoomType;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.repository.ChatMessageRepository;
import nbc.devmountain.domain.chat.model.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	@Transactional
	public ChatMessageResponse createMessage(User user, Long chatRoomId, String message) {
		//채팅방을 지정하지 않았을 때 새로운 채팅방 생성
		ChatRoom chatRoom;

		if (chatRoomId != null) {
			chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

			if (!chatRoom.getUser().getUserId().equals(user.getUserId())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}

		} else {
			ChatRoom newChatRoom = ChatRoom.builder()
				.user(user)
				.chatroomName("채팅방 : " + LocalDate.now().toString())
				.type(RoomType.valueOf(user.getMembershipLevel().name()))
				.build();

			chatRoom = chatRoomRepository.save(newChatRoom);

		}
		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(user)
			.message(message)
			.isAiResponse(false)
			.build();
		chatRoom.addMessages(chatMessage);
		return ChatMessageResponse.from(chatMessageRepository.save(chatMessage));
	}
}
