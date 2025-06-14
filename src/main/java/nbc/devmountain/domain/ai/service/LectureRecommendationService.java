// src/main/java/nbc/devmountain/domain/ai/service/LectureRecommendationService.java
package nbc.devmountain.domain.ai.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.user.model.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureRecommendationService {
	private final RagService ragService;
	private final AiService aiService;

	private final Map<Long, Integer> chatSteps = new ConcurrentHashMap<>();
	private final Map<Long, Map<String, String>> chatResponses = new ConcurrentHashMap<>();

	public ChatMessageResponse recommendationResponse(String query, User.MembershipLevel memberType, Long chatRoomId) {
		if (query == null || query.trim().isEmpty()) {
			log.warn("빈 쿼리 수신: chatRoomId={}", chatRoomId);
			return createErrorResponse("메시지를 입력해주세요.");
		}

		if (chatRoomId == null) {
			log.error("chatRoomId가 null입니다.");
			return createErrorResponse("채팅방 정보를 찾을 수 없습니다.");
		}

		try {
			int currentStep = chatSteps.getOrDefault(chatRoomId, 0);
			return processStep(currentStep, query, chatRoomId);
		} catch (Exception e) {
			log.error("강의 추천 처리 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
			resetChatState(chatRoomId);
			return createErrorResponse("처리 중 오류가 발생했습니다. 다시 시작해주세요.");
		}
	}

	private ChatMessageResponse processStep(int currentStep, String query, Long chatRoomId) {
		switch (currentStep) {
			case 0:
				return handleFirstMessage(chatRoomId);
			case 1:
				return handleInterestStep(query, chatRoomId);
			case 2:
				return handleLevelStep(query, chatRoomId);
			case 3:
				return handleGoalStep(query, chatRoomId);
			default:
				resetChatState(chatRoomId);
				return createErrorResponse("대화가 초기화되었습니다. 다시 시작해주세요.");
		}
	}

	private ChatMessageResponse handleFirstMessage(Long chatRoomId) {
		chatSteps.put(chatRoomId, 1);
		return createSimpleMessage(
			"안녕하세요! DevMountain 챗봇입니다.\n" +
				"어떤 분야의 강의를 찾고 계신가요?\n" +
				"예시: '자바', '스프링', '프론트엔드' 등"
		);
	}

	private ChatMessageResponse handleInterestStep(String query, Long chatRoomId) {
		if (query.length() < 2) {
			return createErrorResponse("더 구체적인 관심사를 입력해주세요.");
		}

		chatResponses.computeIfAbsent(chatRoomId, k -> new HashMap<>())
			.put("interest", query);
		chatSteps.put(chatRoomId, 2);

		return createSimpleMessage(
			"어떤 난이도의 강의를 원하시나요?\n" +
				"초급/중급/고급 중 선택해주세요."
		);
	}

	private ChatMessageResponse handleLevelStep(String query, Long chatRoomId) {
		if (!isValidLevel(query)) {
			return createErrorResponse("초급, 중급, 고급 중에서 선택해주세요.");
		}

		chatResponses.get(chatRoomId).put("level", query);
		chatSteps.put(chatRoomId, 3);

		return createSimpleMessage(
			"어떤 목표로 공부하시나요?\n" +
				"예시: '취업 준비', '실무 적용', '기초 학습' 등"
		);
	}

	private ChatMessageResponse handleGoalStep(String query, Long chatRoomId) {
		if (query.length() < 2) {
			return createErrorResponse("더 구체적인 목표를 입력해주세요.");
		}

		Map<String, String> responses = chatResponses.get(chatRoomId);
		responses.put("goal", query);

		try {
			List<Lecture> similarLectures = ragService.searchSimilarLectures(
				String.format("%s %s %s",
					responses.get("interest"),
					responses.get("level"),
					responses.get("goal"))
			);

			if (similarLectures.isEmpty()) {
				resetChatState(chatRoomId);
				return createErrorResponse("조건에 맞는 강의를 찾지 못했습니다. 다시 시도해주세요.");
			}

			String lectureInfo = similarLectures.stream()
				.map(l -> "제목: %s, 설명: %s".formatted(l.getTitle(), l.getDescription()))
				.collect(Collectors.joining("\n"));

			String promptText = String.format(
				"[사용자 관심사]\n%s\n[희망 난이도]\n%s\n[학습 목표]\n%s\n\n[유사한 강의 정보]\n%s",
				responses.get("interest"),
				responses.get("level"),
				responses.get("goal"),
				lectureInfo
			);

			resetChatState(chatRoomId);
			return aiService.getRecommendations(promptText, true);

		} catch (Exception e) {
			log.error("강의 검색 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
			resetChatState(chatRoomId);
			return createErrorResponse("강의 검색 중 오류가 발생했습니다. 다시 시도해주세요.");
		}
	}

	private boolean isValidLevel(String level) {
		return level != null &&
			(level.contains("초급") || level.contains("중급") || level.contains("고급"));
	}

	private void resetChatState(Long chatRoomId) {
		chatSteps.remove(chatRoomId);
		chatResponses.remove(chatRoomId);
	}

	private ChatMessageResponse createSimpleMessage(String message) {
		return ChatMessageResponse.builder()
			.message(message)
			.isAiResponse(true)
			.messageType(ChatMessageResponse.MessageType.CHAT)
			.build();
	}

	private ChatMessageResponse createErrorResponse(String errorMessage) {
		return ChatMessageResponse.builder()
			.message(errorMessage)
			.isAiResponse(true)
			.messageType(ChatMessageResponse.MessageType.ERROR)
			.build();
	}
}