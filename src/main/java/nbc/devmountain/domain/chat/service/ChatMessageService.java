package nbc.devmountain.domain.chat.service;

import java.util.ArrayList;
import java.util.Collections;
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
import nbc.devmountain.domain.lecture.model.WebSearch;
import nbc.devmountain.domain.lecture.model.Youtube;
import nbc.devmountain.domain.lecture.repository.WebSearchRepository;
import nbc.devmountain.domain.lecture.repository.YoutubeRepository;
import nbc.devmountain.domain.recommendation.dto.RecommendationDto;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.chat.repository.ChatMessageRepository;
import nbc.devmountain.domain.chat.repository.ChatRoomRepository;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;
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
	private final RecommendationRepository recommendationRepository;
	private final LectureRepository lectureRepository;
	private final YoutubeRepository youtubeRepository;
	private final WebSearchRepository webSearchRepository;

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
		ChatMessage savedMsg = chatMessageRepository.save(chatMessage);

		return ChatMessageResponse.from(savedMsg);
	}

	@Transactional
	public ChatMessageResponse createAIMessage(Long chatRoomId, ChatMessageResponse aiResponse) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		User user = chatRoom.getUser();

		try {
			String messageContent;
			MessageType messageType = aiResponse.getMessageType();
			ChatMessage aiChatMessage;

			if (messageType == MessageType.RECOMMENDATION && aiResponse.getRecommendations() != null
				&& !aiResponse.getRecommendations().isEmpty()) {

				// 추천 정보 직렬화
				messageContent = objectMapper.writeValueAsString(aiResponse.getRecommendations());
				log.info("추천 데이터 JSON 직렬화 완료: {}", messageContent);

				// 메시지 저장
				aiChatMessage = ChatMessage.builder()
					.chatRoom(chatRoom)
					.user(null)
					.message(messageContent)
					.isAiResponse(true)
					.messageType(MessageType.RECOMMENDATION)
					.build();
				chatRoom.addMessages(aiChatMessage);
				ChatMessage savedChatMessage = chatMessageRepository.save(aiChatMessage);

				List<RecommendationDto> savedRecommendations = new ArrayList<>();

				for (RecommendationDto recDto : aiResponse.getRecommendations()) {
					Lecture lecture = null;
					Youtube youtube = null;
					WebSearch webSearch = null;
					Recommendation.LectureType lectureType = null;

					if (recDto.lectureId() != null) {
						//기존강의
						lecture = lectureRepository.findById(recDto.lectureId()).orElse(null);
						lectureType = Recommendation.LectureType.VECTOR;
					} else if ("YOUTUBE".equalsIgnoreCase(recDto.type())) {
						lectureType = Recommendation.LectureType.YOUTUBE;
						//유튜부 강의 정보 저장
						 youtube = youtubeRepository.findByUrl(recDto.url());
						if (youtube == null) {
							youtube = Youtube.builder()
								.title(recDto.title())
								.description(recDto.description())
								.url(recDto.url())
								.thumbnailUrl(recDto.thumbnailUrl())
								.build();
							youtubeRepository.save(youtube);
						}
					} else if ("BRAVE".equalsIgnoreCase(recDto.type())) {
						lectureType = Recommendation.LectureType.BRAVE;
						 webSearch = webSearchRepository.findByUrl(recDto.url());
						if(webSearch==null){
							webSearch = WebSearch.builder()
								.title(recDto.title())
								.description(recDto.description())
								.url(recDto.url())
								.build();
							webSearchRepository.save(webSearch);
						}

					}

					Recommendation recommendation = Recommendation.builder()
						.chatMessage(savedChatMessage)
						.user(user)
						.lecture(lecture)
						.webSearch(webSearch)
						.youtube(youtube)
						.score(recDto.score())
						.type(lectureType)
						.build();
					recommendationRepository.save(recommendation);

					savedRecommendations.add(recDto); // 실제 저장된 데이터를 수집
				}

				return ChatMessageResponse.builder()
					.chatroomId(savedChatMessage.getChatRoom().getChatroomId())
					.chatId(savedChatMessage.getChatId())
					.userId(null)
					.message(messageContent)
					.recommendations(savedRecommendations)
					.isAiResponse(true)
					.messageType(MessageType.RECOMMENDATION)
					.createdAt(savedChatMessage.getCreatedAt())
					.updatedAt(savedChatMessage.getUpdatedAt())
					.build();
			} else {
				// 일반 메시지 처리
				messageContent = aiResponse.getMessage();
				aiChatMessage = ChatMessage.builder()
					.chatRoom(chatRoom)
					.user(null)
					.message(messageContent)
					.isAiResponse(true)
					.messageType(messageType)
					.build();

				chatRoom.addMessages(aiChatMessage);
				ChatMessage savedChatMessage = chatMessageRepository.save(aiChatMessage);

				return ChatMessageResponse.builder()
					.chatroomId(savedChatMessage.getChatRoom().getChatroomId())
					.chatId(savedChatMessage.getChatId())
					.userId(null)
					.message(messageContent)
					.recommendations(Collections.emptyList())
					.isAiResponse(true)
					.messageType(messageType)
					.createdAt(savedChatMessage.getCreatedAt())
					.updatedAt(savedChatMessage.getUpdatedAt())
					.build();
			}
		} catch (JsonProcessingException e) {
			log.error("AI 메시지 생성 중 오류 발생: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 메시지 생성 실패");
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