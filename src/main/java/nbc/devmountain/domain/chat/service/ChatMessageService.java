package nbc.devmountain.domain.chat.service;

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
import nbc.devmountain.domain.ai.dto.RecommendationDto;
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
	public ChatMessageResponse createAIMessage(Long chatId,Long chatRoomId, ChatMessageResponse aiResponse) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		ChatMessage chatMessage = chatMessageRepository.findById(chatId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		User user = chatRoom.getUser();

		try {
			String messageContent;
			MessageType messageType;
			if (aiResponse.getRecommendations() != null && !aiResponse.getRecommendations().isEmpty()) {
				messageContent = objectMapper.writeValueAsString(aiResponse.getRecommendations());
				messageType = MessageType.RECOMMENDATION;

				log.info("추천 데이터 JSON 직렬화 완료: {}", messageContent);
				// 추천 기록 저장
				for (RecommendationDto recDto : aiResponse.getRecommendations()) {

					Lecture lecture = null;
					lecture = lectureRepository.findById(recDto.lectureId()).orElse(null);
					log.info("lectureId {}로 강의 검색 결과: {}", recDto.lectureId(), lecture != null ? "성공" : "실패");
					//null일 경우 제목,강사로 가져오기
					if (lecture == null && recDto.title() != null) {
						List<Lecture> lectures;

						if (recDto.instructor() != null) {
							lectures = lectureRepository.findByTitleAndInstructor(recDto.title(), recDto.instructor());
						} else {
							lectures = lectureRepository.findByTitle(recDto.title());
						}
						lecture = lectures.get(0);
					}
					//todo:추천정보 유사도값 넣어야함
					Recommendation recommendation = Recommendation.builder()
						.chatMessage(chatMessage)
						.user(user)
						.lecture(lecture)
						.score(null)
						.build();
					// Recommendation 엔티티 저장
					recommendationRepository.save(recommendation);
					log.info("강의 추천 기록 저장 성공: lectureId={}, userId={}", lecture.getLectureId(), user.getUserId());
				}
			} else {
				// 일반 AI 메시지인 경우, message 필드 사용
				messageContent = aiResponse.getMessage();
				messageType = aiResponse.getMessageType();
			}

			ChatMessage aiChatMessage = ChatMessage.builder()
				.chatRoom(chatRoom)
				.user(null)
				.message(messageContent)
				.isAiResponse(true)
				.messageType(messageType)
				.build();

			chatRoom.addMessages(aiChatMessage);

			ChatMessage savedChatMessage = chatMessageRepository.save(aiChatMessage);
			log.info("AI 메시지 생성 완료 - 타입: {}", aiResponse.getMessageType());

			return ChatMessageResponse.from(savedChatMessage);

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
