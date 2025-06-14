// src/main/java/nbc/devmountain/domain/ai/service/AiService.java
package nbc.devmountain.domain.ai.service;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.chatmessage.dto.response.ChatMessageResponse;

@Service
@AllArgsConstructor
@Slf4j
public class AiService {
	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	public ChatMessageResponse getRecommendations(String promptText, boolean isFinalStep) {
		SystemMessage systemMessage = new SystemMessage(
			isFinalStep ?
				"""
				너는 주어진 정보를 바탕으로 강의를 추천하는 교육 큐레이터 AI야.
				- 사용자의 질문과 제공된 '유사한 강의 정보'를 바탕으로 가장 적절한 강의를 최대 3개 추천해줘.
				- 각 강의는 title, url, level, thumbnailUrl 을 포함해야 해.
				- 응답은 반드시 JSON 형식으로만 해야하며, 절대로 JSON 객체 외의 다른 텍스트(예: 설명, 인사)를 포함하면 안돼.
				- 만약 추천할 강의가 없다면, recommendations 배열을 비워서 보내줘. 예: {"recommendations": []}
				- 응답 예시:
				{
				"recommendations": [
				  {
				    "title": "자바 입문",
				    "description": "...",
				    "instructor": "...",
				    "level": "초급",
				    "thumbnailUrl": "..."
				  }
				 ]
				}
				"""
				:
				"""
				너는 교육 큐레이터 AI야. 사용자와의 대화를 통해 강의를 추천하기 위한 정보를 수집하고 있어.
				- 사용자의 응답에 대해 자연스럽게 대화하듯이 답변해줘.
				- JSON 형식으로 응답할 필요 없이, 자연스러운 대화형 메시지를 반환해줘.
				- 다음 단계로 넘어가기 위한 안내 메시지를 포함해줘.
				"""
		);

		Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(promptText)));
		log.info("[AiService] 프롬프트 전송 >>>\n{}", promptText);

		ChatResponse response = chatModel.call(prompt);
		String rawAiResponse = response.getResults()
			.stream()
			.findFirst()
			.map(result -> result.getOutput().getContent())
			.orElse("");

		log.info("[AiService] AI 응답(원본) >>>\n{}", rawAiResponse);

		if (!isFinalStep) {
			return ChatMessageResponse.builder()
				.message(rawAiResponse)
				.isAiResponse(true)
				.messageType(ChatMessageResponse.MessageType.CHAT)
				.build();
		}

		String pureJson = extractJsonString(rawAiResponse);

		if (pureJson.isEmpty()) {
			log.warn("[AiService] AI 응답에서 JSON을 찾을 수 없음.");
			return createErrorResponse("AI가 응답을 생성하지 못했습니다. 다시 시도해주세요.");
		}

		try {
			JsonNode root =objectMapper.readTree(pureJson);
			JsonNode recNode = root.get("recommendations");
			List<RecommendationDto> recommendations = objectMapper.readValue(
				recNode.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, RecommendationDto.class));

			if (recommendations == null || recommendations.isEmpty()) {
				log.info("[AiService] AI가 추천할 강의를 찾지 못함.");
				return createErrorResponse("아쉽지만, 현재 조건에 맞는 강의를 찾지 못했어요. 질문을 조금 더 구체적으로 해주시겠어요?");
			}

			log.info("[AiService] AI 추천 결과 파싱 성공: {}", recommendations);
			return ChatMessageResponse.builder()
				.message(null)
				.recommendations(recommendations)
				.isAiResponse(true)
				.messageType(ChatMessageResponse.MessageType.RECOMMENDATION)
				.build();

		} catch (JsonProcessingException e) {
			log.error("[AiService] AI 응답 파싱 실패!\n원본: {}\n추출된 JSON: {}\n에러: {}",
				rawAiResponse, pureJson, e.toString());
			return createErrorResponse("AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
	}

	private String extractJsonString(String rawResponse) {
		if (rawResponse == null || rawResponse.trim().isEmpty()) {
			return "";
		}
		Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(rawResponse);

		if (matcher.find()) {
			return matcher.group();
		}
		return "";
	}

	private ChatMessageResponse createErrorResponse(String errorMessage) {
		return ChatMessageResponse.builder()
			.message(errorMessage)
			.recommendations(Collections.emptyList())
			.isAiResponse(true)
			.messageType(ChatMessageResponse.MessageType.ERROR)
			.build();
	}
}