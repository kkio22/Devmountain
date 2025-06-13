package nbc.devmountain.domain.chat.chatmessage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.chatmessage.repository.ChatMessageRepository;
import nbc.devmountain.domain.chat.chatroom.repository.ChatRoomRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	@Transactional
	public ChatMessageResponse createMessage(Long userId, Long chatRoomId, String message) {

		if (message == null || message.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메시지 내용이 비어있습니다.");
		}
		if (message.length() > 1000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메시지가 너무 깁니다. (최대 1000자)");
		}
		//삭제된 채팅방은 메세지입력 x

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(user.getUserId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
	@Transactional
	public ChatMessageResponse createAIMessage(Long chatRoomId,String message){
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		ChatMessage aiChatMessage = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(null)
			.message(message)
			.isAiResponse(true)
			.build();
		chatRoom.addMessages(aiChatMessage);
		log.info("메세지 생성 완료");

		return ChatMessageResponse.from(chatMessageRepository.save(aiChatMessage));
	}



	public List<ChatMessageResponse> getMessages(Long userId, Long roomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (!chatRoom.getUser().getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(roomId);

		return chatMessages.stream()
			.map(ChatMessageResponse::from)
			.collect(Collectors.toList());


	}

	// // todo : 채팅 기능 용 임시
	// public List<ChatMessageResponse> getMessages(Long userId, Long roomId) {
	// 	// 1) 채팅방 존재 여부 확인
	// 	var chatRoomOpt = chatRoomRepository.findById(roomId);
	//
	// 	if (chatRoomOpt.isEmpty()) {
	// 		// DB에 없는 방 (비회원 방) → 권한 체크 불필요, 빈 리스트 반환 or 메모리 저장 메시지 반환
	// 		return Collections.emptyList();
	// 	}
	//
	// 	ChatRoom chatRoom = chatRoomOpt.get();
	//
	// 	// 2) 회원 전용 권한 체크 (userId가 null이거나 0이라면 건너뛸 수도 있음)
	// 	if (userId == null || !chatRoom.getUser().getUserId().equals(userId)) {
	// 		throw new ResponseStatusException(HttpStatus.FORBIDDEN);
	// 	}
	//
	// 	// 3) 메시지 조회 및 반환
	// 	List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(roomId);
	//
	// 	return chatMessages.stream()
	// 		.map(ChatMessageResponse::from)
	// 		.collect(Collectors.toList());
	// }

}
