package nbc.devmountain.websocket.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGPTService {

	@Value("${OPENAI_API_KEY}")
	private String apiKey;

	@Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
	private String apiUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	public String generateResponse(String systemPrompt, List<ChatMessageResponse> history, String userInput) {
		try {
			List<Map<String, String>> messages = buildMessages(systemPrompt, history, userInput);
			return callChatGPTAPI(messages);
		} catch (Exception e) {
			log.error("ChatGPT API 호출 실패", e);
			return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
		}
	}

	public String generateGuestResponse(String userInput) {
		try {
			List<Map<String, String>> messages = new ArrayList<>();

			String guestPrompt = buildGuestSystemPrompt();
			messages.add(Map.of("role", "system", "content", guestPrompt));
			messages.add(Map.of("role", "user", "content", userInput));

			return callChatGPTAPI(messages);
		} catch (Exception e) {
			log.error("ChatGPT API 호출 실패 (비회원)", e);
			return "안녕하세요! 간단한 질문에 답변드릴 수 있어요. 더 자세한 상담을 원하시면 로그인해주세요.";
		}
	}

	private List<Map<String, String>> buildMessages(String systemPrompt, List<ChatMessageResponse> history,
		String userInput) {
		List<Map<String, String>> messages = new ArrayList<>();

		// 시스템 프롬프트
		messages.add(Map.of("role", "system", "content", systemPrompt));

		// 대화 히스토리 (최근 10개만)
		if (history != null && !history.isEmpty()) {
			int startIndex = Math.max(0, history.size() - 10);

			for (int i = startIndex; i < history.size(); i++) {
				ChatMessageResponse msg = history.get(i);
				String role = msg.isAiResponse() ? "assistant" : "user";
				messages.add(Map.of("role", role, "content", msg.message()));
			}
		}

		messages.add(Map.of("role", "user", "content", userInput));

		return messages;
	}

	private String callChatGPTAPI(List<Map<String, String>> messages) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", "gpt-4o-mini");
		requestBody.put("messages", messages);
		requestBody.put("max_tokens", 1000);
		requestBody.put("temperature", 0.7);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);
		Map<String, Object> responseBody = response.getBody();

		List<Map<String, Object>> choices = (List<Map<String, Object>>)responseBody.get("choices");
		Map<String, Object> message = (Map<String, Object>)choices.get(0).get("message");

		return ((String)message.get("content")).trim();
	}

	private String buildGuestSystemPrompt() {
		return "당신은 온라인 강의 플랫폼의 AI 상담사입니다.\n\n" +
			"현재 사용자는 비회원입니다.\n\n" +
			"지침:\n" +
			"1. 친근하고 도움이 되는 톤으로 대화하세요\n" +
			"2. 간단한 학습 관련 질문에는 일반적인 조언을 제공하세요\n" +
			"3. 구체적인 강의 추천 요청이 있으면 'RECOMMEND_COURSES: [요구사항]' 형식으로 응답하세요\n" +
			"4. 더 자세한 상담이나 개인 맞춤 추천을 원한다면 로그인이 필요하다는 것을 자연스럽게 안내하세요\n";
	}
}

