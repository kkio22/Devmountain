package nbc.devmountain.domain.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.user.model.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureRecommendationService {
	private final RagService ragService;
	private final AiService aiService;

	// 대화 히스토리를 저장 (chatRoomId -> 대화 내용들)
	private final Map<Long, StringBuilder> conversationHistory = new ConcurrentHashMap<>();
	// 수집된 정보 저장 (chatRoomId -> 수집된 정보)
	private final Map<Long, Map<String, String>> collectedInfo = new ConcurrentHashMap<>();

	public ChatMessageResponse recommendationResponse(String query, User.MembershipLevel memberType, Long chatRoomId) {
		if (query == null || query.trim().isEmpty()) {
			log.warn("빈 쿼리 수신: chatRoomId={}", chatRoomId);
			return createErrorResponse(AiConstants.ERROR_EMPTY_QUERY);
		}

		if (chatRoomId == null) {
			log.error("chatRoomId가 null입니다.");
			return createErrorResponse(AiConstants.ERROR_NO_CHATROOM);
		}

		try {
			return processConversation(query, chatRoomId);
		} catch (Exception e) {
			log.error("강의 추천 처리 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
			resetChatState(chatRoomId);
			return createErrorResponse(AiConstants.ERROR_PROCESSING_FAILED);
		}
	}

	private ChatMessageResponse processConversation(String userMessage, Long chatRoomId) {
		// 대화 히스토리 업데이트
		StringBuilder history = conversationHistory.computeIfAbsent(chatRoomId, k -> new StringBuilder());
		history.append("사용자: ").append(userMessage).append("\n");

		// 수집된 정보 맵 초기화
		Map<String, String> info = collectedInfo.computeIfAbsent(chatRoomId, k -> new HashMap<>());

		// 첫 번째 메시지인 경우
		if (history.toString().trim().equals("사용자: " + userMessage)) {
			return handleFirstConversation(userMessage, chatRoomId);
		}

		// AI에게 대화 분석 및 다음 단계 결정 요청
		ChatMessageResponse analysisResponse = aiService.analyzeConversationAndDecideNext(
			history.toString(), 
			info, 
			userMessage
		);

		// AI 응답을 히스토리에 추가
		if (analysisResponse.getMessage() != null) {
			history.append("AI: ").append(analysisResponse.getMessage()).append("\n");
		}

		// 충분한 정보가 수집되었는지 확인
		if (analysisResponse.getMessageType() == MessageType.RECOMMENDATION) {
			// 최종 추천 단계 - RAG 검색 및 추천 생성
			return generateFinalRecommendation(info, chatRoomId);
		}

		return analysisResponse;
	}

	private ChatMessageResponse handleFirstConversation(String userMessage, Long chatRoomId) {
		// 첫 대화에서도 AI가 자연스럽게 응답하도록 처리
		Map<String, String> emptyInfo = new HashMap<>();
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			"사용자: " + userMessage + "\n", 
			emptyInfo, 
			userMessage
		);

		// AI 응답을 히스토리에 추가
		if (response.getMessage() != null) {
			conversationHistory.get(chatRoomId).append("AI: ").append(response.getMessage()).append("\n");
		}

		return response;
	}

	private ChatMessageResponse generateFinalRecommendation(Map<String, String> collectedInfo, Long chatRoomId) {
		try {
			// 수집된 정보로 검색 쿼리 생성
			String searchQuery = buildSearchQuery(collectedInfo);
			
			List<Lecture> similarLectures = ragService.searchSimilarLectures(searchQuery);

			if (similarLectures.isEmpty()) {
				resetChatState(chatRoomId);
				return createErrorResponse(AiConstants.ERROR_NO_LECTURES_FOUND);
			}

			String lectureInfo = similarLectures.stream()
				.map(l -> "강의 ID: %d, 제목: %s, 설명: %s, 강사: %s, 난이도: %s, 썸네일: %s".formatted
					(l.getLectureId(),l.getTitle(), l.getDescription(), l.getInstructor(), l.getLevelCode(),
					l.getThumbnailUrl()))
				.collect(Collectors.joining("\n"));

			String promptText = String.format(
				"[수집된 사용자 정보]\n%s\n\n[유사한 강의 정보]\n%s",
				formatCollectedInfo(collectedInfo),
				lectureInfo
			);

			resetChatState(chatRoomId);
			return aiService.getRecommendations(promptText, true);

		} catch (Exception e) {
			log.error("강의 검색 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
			resetChatState(chatRoomId);
			return createErrorResponse(AiConstants.ERROR_LECTURE_SEARCH_FAILED);
		}
	}

	private String buildSearchQuery(Map<String, String> info) {
		StringBuilder query = new StringBuilder();
		if (info.containsKey(AiConstants.INFO_INTEREST)) {
			query.append(info.get(AiConstants.INFO_INTEREST)).append(" ");
		}
		if (info.containsKey(AiConstants.INFO_LEVEL)) {
			query.append(info.get(AiConstants.INFO_LEVEL)).append(" ");
		}
		if (info.containsKey(AiConstants.INFO_GOAL)) {
			query.append(info.get(AiConstants.INFO_GOAL)).append(" ");
		}
		if (info.containsKey(AiConstants.INFO_ADDITIONAL)) {
			query.append(info.get(AiConstants.INFO_ADDITIONAL)).append(" ");
		}
		return query.toString().trim();
	}

	private String formatCollectedInfo(Map<String, String> info) {
		StringBuilder formatted = new StringBuilder();
		if (info.containsKey(AiConstants.INFO_INTEREST)) {
			formatted.append(AiConstants.LABEL_INTEREST)
				.append(": ")
				.append(info.get(AiConstants.INFO_INTEREST))
				.append("\n");
		}
		if (info.containsKey(AiConstants.INFO_LEVEL)) {
			formatted.append(AiConstants.LABEL_LEVEL)
				.append(": ")
				.append(info.get(AiConstants.INFO_LEVEL))
				.append("\n");
		}
		if (info.containsKey(AiConstants.INFO_GOAL)) {
			formatted.append(AiConstants.LABEL_GOAL).append(": ").append(info.get(AiConstants.INFO_GOAL)).append("\n");
		}
		if (info.containsKey(AiConstants.INFO_ADDITIONAL)) {
			formatted.append(AiConstants.LABEL_ADDITIONAL)
				.append(": ")
				.append(info.get(AiConstants.INFO_ADDITIONAL))
				.append("\n");
		}
		return formatted.toString();
	}

	private void resetChatState(Long chatRoomId) {
		conversationHistory.remove(chatRoomId);
		collectedInfo.remove(chatRoomId);
	}

	private ChatMessageResponse createErrorResponse(String errorMessage) {
		return ChatMessageResponse.builder()
			.message(errorMessage)
			.isAiResponse(true)
			.messageType(MessageType.ERROR)
			.build();
	}
}
