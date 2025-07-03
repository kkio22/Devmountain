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
import nbc.devmountain.domain.recommendation.model.RecommendationCount;
import nbc.devmountain.domain.recommendation.repository.RecommendationCountRepository;
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
	private final RecommendationCountRepository recommendationCountRepository;

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

					try {
						Recommendation.LectureType typeEnum = Recommendation.LectureType.valueOf(
							(recDto.type() != null ? recDto.type() : "VECTOR").toUpperCase()
						);

						switch (typeEnum) {
							case VECTOR -> {
								if (recDto.lectureId() == null) {
									log.warn("lectureId가 null 입니다. 추천 기록에 저장하지 않음.");
									continue;
								}
								lecture = lectureRepository.findById(recDto.lectureId()).orElse(null);
								if (lecture == null) {
									log.warn("VECTOR 강의 ID={}를 찾을 수 없습니다.", recDto.lectureId());
									continue;
								}
								lectureType = Recommendation.LectureType.VECTOR;
								log.info("VECTOR 무료 강의 추천 기록 저장: {} (ID={})", lecture.getTitle(), lecture.getLectureId());
							}
							case YOUTUBE -> {
								lectureType = Recommendation.LectureType.YOUTUBE;
								youtube = youtubeRepository.findByUrl(recDto.url());
								if (youtube == null) {
									youtube = Youtube.builder()
										.title(recDto.title())
										.description(recDto.description())
										.url(recDto.url())
										.thumbnailUrl(recDto.thumbnailUrl())
										.build();
									youtubeRepository.save(youtube);
									log.info("YOUTUBE 강의 새로 저장: {}", recDto.title());
								} else {
									log.info("YOUTUBE 강의 이미 존재: {}", recDto.title());
								}
							}
							case BRAVE -> {
								lectureType = Recommendation.LectureType.BRAVE;
								webSearch = webSearchRepository.findByUrl(recDto.url());
								if (webSearch == null) {
									webSearch = WebSearch.builder()
										.title(recDto.title())
										.description(recDto.description())
										.url(recDto.url())
										.thumbnailUrl(recDto.thumbnailUrl())
										.build();
									webSearchRepository.save(webSearch);
									log.info("BRAVE 강의 새로 저장: {}", recDto.title());
								} else {
									log.info("BRAVE 강의 이미 존재: {}", recDto.title());
								}
							}
						}

						Recommendation recommendation = Recommendation.builder()
							.chatMessage(savedChatMessage)
							.user(user)
							.lecture(lecture)
							.youtube(youtube)
							.webSearch(webSearch)
							.score(recDto.score())
							.type(lectureType)
							.build();
						recommendationRepository.save(recommendation);

						if (lecture != null) {
							Lecture finalLecture = lecture;
							RecommendationCount count = recommendationCountRepository.findByLecture(lecture)
								.orElseGet(() -> RecommendationCount.builder().lecture(finalLecture).count(0L).build());
							count.increase();
							recommendationCountRepository.save(count);
						}
						if (youtube != null) {
							Youtube finalYoutube = youtube;
							RecommendationCount count = recommendationCountRepository.findByYoutube(youtube)
								.orElseGet(() -> RecommendationCount.builder().youtube(finalYoutube).count(0L).build());
							count.increase();
							recommendationCountRepository.save(count);
						}
						if (webSearch != null) {
							WebSearch finalWebSearch = webSearch;
							RecommendationCount count = recommendationCountRepository.findByWebSearch(webSearch)
								.orElseGet(() -> RecommendationCount.builder().webSearch(finalWebSearch).count(0L).build());
							count.increase();
							recommendationCountRepository.save(count);
						}

				log.info("추천 기록 저장 완료 - type: {}, title: {}", typeEnum, recDto.title());
				savedRecommendations.add(recDto);

					} catch (IllegalArgumentException | NullPointerException e) {
						log.warn("알 수 없는 타입: {} (title: {})", recDto.type(), recDto.title());
						continue; // 혹은 예외 던지기
					}
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