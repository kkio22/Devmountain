package nbc.devmountain.domain.chat.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.RoomType;
import nbc.devmountain.domain.chat.dto.ChatRoomDetailResponse;
import nbc.devmountain.domain.chat.dto.ChatRoomResponse;
import nbc.devmountain.domain.chat.repository.ChatRoomRepository;
import nbc.devmountain.domain.chat.websocket.WebSocketMessageSender;
import nbc.devmountain.domain.chat.websocket.WebSocketSessionManager;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final WebSocketMessageSender messageSender;
	private final WebSocketSessionManager webSocketSessionManager;

	@Transactional
	public ChatRoomResponse createChatRoom(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		ChatRoom chatRoom = ChatRoom.builder()
			.user(user)
			.chatroomName("새 채팅방")
			.type(RoomType.valueOf(user.getMembershipLevel().name()))
			.build();

		ChatRoom saveRoom = chatRoomRepository.save(chatRoom);

		log.info("채팅방 생성 완료 - userId: {}, roomId: {}",
			userId, saveRoom.getChatroomId());

		return ChatRoomResponse.from(saveRoom);
	}

	@Transactional(readOnly = true)
	public List<ChatRoomResponse> findAllChatRooms(Long userId) {

		List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);

		return chatRooms.stream()
			.map(ChatRoomResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public ChatRoomDetailResponse findChatRoom(Long userId, Long chatroomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		return ChatRoomDetailResponse.from(chatRoom);
	}

	@Transactional
	public ChatRoomResponse updateChatRoomName(Long userId, Long chatroomId, String newName){
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		chatRoom.updateName(newName);
		ChatRoomResponse updatedRoomName = ChatRoomResponse.from(chatRoom);
		log.info("채팅방 이름 변경완료 - userId: {},chatroomId: {},newName: {}", userId, chatroomId, newName);
		messageSender.sendUpdateRoomName(chatroomId,newName);
		return updatedRoomName;
	}

	@Transactional
	public void deleteChatRoom(Long userId, Long chatroomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		chatRoom.delete();
		log.info("채팅방 삭제 완료 - userId: {}, roomId: {}", userId, chatroomId);
		webSocketSessionManager.removeRoomSessions(chatroomId);
	}

	@Transactional(readOnly = true)
	public ChatRoom getChatRoomOrThrow(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
}
