package nbc.devmountain.domain.ai.service;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;

@Service
@AllArgsConstructor
@Slf4j
public class AiService {

	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	public AiRecommendationResponse getRecommendations(String promptText) {
		// 시스템 역할 정의 (AI에게 어떤 역할을 맡겼는지 설명)
		SystemMessage systemMessage = new SystemMessage(
			"너는 교육 큐레이터 AI야." +
				"사용자의 관심사, 현재 수준, 학습 목표를 참고해서 적절한 강의 3개를 추천해줘." +
				"각 강의는 title, url, level(초급/중급/고급)을 포함해야 해." +
				"응답은 반드시 JSON 형식으로 해줘."+
				"예시:\n" +
				"{\n" +
				"  \"recommendations\": [\n" +
				"    {\"title\": \"스프링 입문\", \"url\": \"https://inflearn.com/spring\", \"level\": \"초급\", \"thumbnailUrl\": null },\n" +
				"    ...\n" +
				"  ]\n" +
				"}"
		);
		// 사용자 정보 입력 + 출력 형식
		// String promptText = """
		// 	[사용자 정보]
		// 	%s
		//
		// 	[유사한 강의 정보]
		// 	%s
		//
		// 	응답 형식 예시:
		// 	{
		// 	  "recommendations": [
		// 	    {
		// 	      "title": "실전 자바 백엔드 개발",
		// 	      "url": "https://example.com/java-backend",
		// 	      "level": "중급"
		// 	    },
		// 	    {
		// 	      "title": "초보자를 위한 Spring Boot",
		// 	      "url": "https://example.com/spring-boot-basic",
		// 	      "level": "초급"
		// 	    },
		// 	    {
		// 	      "title": "고급 API 설계와 보안",
		// 	      "url": "https://example.com/api-design",
		// 	      "level": "고급"
		// 	    }
		// 	  ]
		// 	}
		// 	""".formatted(userContext, lectureInfo);

		Prompt prompt = new Prompt(List.of(
			systemMessage,
			new UserMessage(promptText)
		));
		log.info("[AiService] 프롬프트 전송 >>>\n{}", promptText);

		// LLM 호출 (gpt-4o-mini 호출 call)
		ChatResponse response = chatModel.call(prompt);
		String aiJson = response.getResults()
			.stream()
			.findFirst()
			.map(result -> result.getOutput().getText())
			.orElseThrow(() -> new RuntimeException("AI 응답이 없습니다."));

		log.info("[AiService] AI 응답(원본 JSON) >>>\n{}", aiJson);

		// 결과를 DTO로 파싱
		try {
			AiRecommendationResponse rec = objectMapper.readValue(aiJson,
				AiRecommendationResponse.class);
			log.info("[AiService] AI 추천 결과 파싱 성공: {}", rec);
			return rec;

		} catch (Exception e) {
			log.error("[AiService] AI 응답 파싱 실패!\n원본: {}\n에러: {}", aiJson, e.toString());
			throw new RuntimeException("AI 응답 파싱 실패: " + aiJson, e);
		}
	}
}
