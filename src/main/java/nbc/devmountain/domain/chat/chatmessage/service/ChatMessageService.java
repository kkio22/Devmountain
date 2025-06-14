package nbc.devmountain.domain.chat.chatmessage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ObjectMapper objectMapper;

	@Transactional
	public ChatMessageResponse createMessage(Long userId, Long chatRoomId, String message) {
		if (message == null || message.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메시지 내용이 비어있습니다.");
		}
		if (message.length() > 1000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메시지가 너무 깁니다. (최대 1000자)");
		}

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
	public ChatMessageResponse createAIMessage(Long chatRoomId, ChatMessageResponse aiResponse) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		try {
			String messageContent;
			if (aiResponse.getRecommendations() != null && !aiResponse.getRecommendations().isEmpty()) {
				// 추천 목록이 있는 경우 JSON으로 직렬화
				messageContent = objectMapper.writeValueAsString(aiResponse.getRecommendations());
			} else {
				// 일반 메시지인 경우 그대로 사용
				messageContent = aiResponse.getMessage();
			}

			ChatMessage aiChatMessage = ChatMessage.builder()
				.chatRoom(chatRoom)
				.user(null)
				.message(messageContent)
				.isAiResponse(true)
				.build();

			chatRoom.addMessages(aiChatMessage);

			log.info("AI 메시지 생성 완료 - 타입: {}", aiResponse.getMessageType());
			return ChatMessageResponse.from(chatMessageRepository.save(aiChatMessage));

		} catch (JsonProcessingException e) {
			log.error("AI 응답 직렬화 실패: {}", e.getMessage());
			throw new RuntimeException("AI 응답을 저장하는 중 오류가 발생했습니다.", e);
		}
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
}