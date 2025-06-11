package nbc.devmountain.domain.ai.service;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class AiService {

	// private final ChatModel chatModel;
	//
	// public String ask(String message) {
	//
	// 	SystemMessage systemMessage = new SystemMessage(
	// 		"너는 교육 큐레이터 AI다. 사용자의 입력에 맞춰서 적절한 강의를 JSON형식으로 추천해줘." +
	// 		"각 강의는 제목(title),URL(url),난이도(Level : 초급/중급/고급)을 포함 해야해."
	// 	);
	//
	// 	/*
	// 	 * UserMessage -> 사용자에게 받은 메세지를 UserMessage로 감싸기
	// 	 * Prompt -> UserMessage를 Prompt 객체에 담아서 사용
	// 	 * chatModel.call(prompt) -> 모델 호출해서 응답 받아온다.
	// 	 * 이때, Spring AI가 HTTP 요청을 자동으로 보내고 응답 받는다.
	// 	 */
	// 	UserMessage userMessage = new UserMessage(
	// 		message + "\n\n응답은 다음 JSON 형식으로 해줘:\n"+
	// 		"{ \"recommendatitons\":[ { \"title\" : \"...\",\"url\": \"...\", \"level\":\"초급\"} ] }"); // Ai에게 이 형식으로 대답을 받기 위해서 작성
	//
	// 	Prompt prompt = new Prompt(List.of(userMessage));
	//
	// 	ChatResponse response = chatModel.call(prompt);
	//
	// 	/*
	// 	 * 응답 중 첫번째 응답을 꺼내서 텍스트만 추출
	// 	 * 응답이 없을 경우, "AI 응답이 없습니다" 메세지를 출력
	// 	 */
	// 	return response.getResults()
	// 		.stream()
	// 		.findFirst()
	// 		.map(result -> result.getOutput().getText())
	// 		.orElse("AI 응답이 없습니다.");
	// }

	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	public AiRecommendationResponse getRecommendations(String interest, String level, String goal) {
		// 시스템 역할 정의 (AI에게 어떤 역할을 맡겼는지 설명)
		SystemMessage systemMessage = new SystemMessage(
			"너는 교육 큐레이터 AI야." +
				"사용자의 관심사, 현재 수준, 학습 목표를 참고해서 적절한 강의 3개를 추천해줘." +
				"각 강의는 title, url, level(초급/중급/고급)을 포함해야 해." +
				"응답은 반드시 JSON 형식으로 해줘."
		);
		// 사용자 정보 입력 + 출력 형식
		String userContext = String.format("""
            사용자 정보:
            - 관심사: %s
            - 현재 수준: %s
            - 학습 목표: %s

            응답 형식 예시:
            {
              "recommendations": [
                {
                  "title": "실전 자바 백엔드 개발",
                  "url": "https://example.com/java-backend",
                  "level": "중급"
                },
                {
                  "title": "초보자를 위한 Spring Boot",
                  "url": "https://example.com/spring-boot-basic",
                  "level": "초급"
                },
                {
                  "title": "고급 API 설계와 보안",
                  "url": "https://example.com/api-design",
                  "level": "고급"
                }
              ]
            }
            """, interest, level, goal);


		UserMessage userMessage = new UserMessage(userContext);
		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

		// LLM 호출 (gpt-4o-mini 호출 call)
		ChatResponse response = chatModel.call(prompt);
		String aiJson = response.getResults()
			.stream()
			.findFirst()
			.map(result -> result.getOutput().getText())
			.orElseThrow(() -> new RuntimeException("AI 응답이 없습니다."));

		// 결과를 DTO로 파싱
		try {
			return objectMapper.readValue(aiJson, AiRecommendationResponse.class);
		} catch (Exception e) {
			throw new RuntimeException("AI 응답 파싱 실패: " + aiJson, e);
		}
	}
}
