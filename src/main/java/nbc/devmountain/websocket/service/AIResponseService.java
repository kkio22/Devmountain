package nbc.devmountain.websocket.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.util.security.SessionUser;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.chatmessage.service.ChatMessageService;
import nbc.devmountain.domain.lecture.service.LectureRecommendationService;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class AIResponseService {
	private final UserRepository userRepository;
	private final ChatMessageService chatMessageService;
	private final ChatGPTService chatGPTService;
	private final LectureRecommendationService lectureRecommendationService;

	public ChatMessageResponse processChat(SessionUser sessionUser, Long roomId, String userInput) {
		// 강의 추천 생성
		User user = userRepository.findById(sessionUser.getUserId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		List<ChatMessageResponse> conversationHistory =
			chatMessageService.getMessages(sessionUser.getUserId(), roomId);

		String aiResponse = chatGPTService.generateResponse(
			buildSystemPrompt(user),
			conversationHistory,
			userInput);

		//강의 추천 답변이오면
		if (aiResponse.startsWith("RECOMMEND_COURSES:")) {
			String requirements = aiResponse.substring("RECOMMEND_COURSES:".length()).trim();
			String recResult = lectureRecommendationService.generateRecommendation(user, requirements);

			return chatMessageService.createAIMessage(roomId, recResult);
		}

		// AI 응답 저장
		return chatMessageService.createAIMessage(roomId, aiResponse);

	}

	public ChatMessageResponse processGuestChat(Long roomId, String message) {

		String aiResponse = chatGPTService.generateGuestResponse(message);

		if (aiResponse.startsWith("RECOMMEND_COURSES:")) {
			String requirements = aiResponse.substring("RECOMMEND_COURSES:".length()).trim();

			// 비회원용 일반적인 강의 추천
			String guestRecommendation = lectureRecommendationService
				.generateGuestRecommendation(requirements);

			return ChatMessageResponse.builder()
				.message(guestRecommendation)
				.isAiResponse(true)
				.build();
		}

		return ChatMessageResponse.builder()
			.message(aiResponse)
			.isAiResponse(true)
			.build();
	}

	private String buildSystemPrompt(User user) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("당신은 온라인 강의 플랫폼의 AI 상담사입니다.\n\n");
		prompt.append("사용자 정보:\n");
		prompt.append("- 이름: ").append(user.getName()).append("\n");
		prompt.append("- 멤버십: ").append(user.getMembershipLevel()).append("\n");
		prompt.append("\n지침:\n");
		prompt.append("1. 친근하고 전문적인 톤으로 사용자와 대화하세요\n");
		prompt.append("2. 사용자의 현재 수준, 학습 목표, 관심 분야를 자연스럽게 파악하세요\n");
		prompt.append("3. 충분한 정보 수집 후 사용자가 구체적인 강의 추천을 원한다면:\n");
		prompt.append("   'RECOMMEND_COURSES: [사용자수준], [관심분야], [학습목표], [기타요구사항]' 형식으로 응답하세요\n");
		prompt.append("4. 예시: 'RECOMMEND_COURSES: 초보자, Java 백엔드 개발, 취업 준비, 실습 위주 선호'\n");
		prompt.append("5. 일반적인 질문이나 상담에는 자연스럽게 응답하세요\n");

		return prompt.toString();
	}
	//비로그인 유저
	private String buildGuestSystemPrompt() {
		return "당신은 온라인 강의 플랫폼의 AI 상담사입니다. " +
			"현재 사용자는 비회원입니다. " +
			"간단한 질문에 답변하고, 강의 추천 요청이 있으면 " +
			"'RECOMMEND_COURSES: [요구사항]' 형식으로 응답하세요. " +
			"더 자세한 상담과 개인 맞춤 추천을 받으려면 로그인이 필요하다는 것을 " +
			"자연스럽게 안내하세요.";
	}
}
