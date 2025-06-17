package nbc.devmountain.domain.ai.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;

@Service
@AllArgsConstructor
@Slf4j
public class AiService {
	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	public ChatMessageResponse analyzeConversationAndDecideNext(String conversationHistory, Map<String, String> collectedInfo, String latestUserMessage) {
		SystemMessage systemMessage = new SystemMessage(AiConstants.CONVERSATION_ANALYSIS_PROMPT);

		String promptText = String.format(
			"현재 대화 히스토리:\n%s\n\n현재 수집된 정보:\n%s\n\n최신 사용자 메시지: %s",
			conversationHistory,
			formatCollectedInfo(collectedInfo),
			latestUserMessage
		);

		Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(promptText)));
		log.info("[AiService] 대화 분석 프롬프트 전송 >>>\n{}", promptText);

		ChatResponse response = chatModel.call(prompt);
		String aiResponse = response.getResults()
			.stream()
			.findFirst()
			.map(result -> result.getOutput().getContent())
			.orElse("");

		log.info("[AiService] 대화 분석 AI 응답 >>>\n{}", aiResponse);

		// AI 기반 정보 추출 및 업데이트
		extractAndUpdateInfoByAI(collectedInfo, latestUserMessage);

		// 추천 준비 완료 확인
		if (aiResponse.contains(AiConstants.READY_FOR_RECOMMENDATION) || isReadyForRecommendation(collectedInfo)) {
			return ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();
		}

		return ChatMessageResponse.builder()
			.message(aiResponse.replace(AiConstants.READY_FOR_RECOMMENDATION, "").trim())
			.isAiResponse(true)
			.messageType(MessageType.CHAT)
			.build();
	}

	private void extractAndUpdateInfoByAI(Map<String, String> collectedInfo, String userMessage) {
		SystemMessage systemMessage = new SystemMessage(AiConstants.INFO_CLASSIFICATION_PROMPT);
		
		String promptText = String.format(
			"사용자 메시지: %s\n\n현재 수집된 정보:\n%s",
			userMessage,
			formatCollectedInfo(collectedInfo)
		);

		Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(promptText)));
		log.info("[AiService] 정보 분류 프롬프트 전송 >>>\n{}", promptText);

		try {
			ChatResponse response = chatModel.call(prompt);
			String aiResponse = response.getResults()
				.stream()
				.findFirst()
				.map(result -> result.getOutput().getContent())
				.orElse("");

			log.info("[AiService] 정보 분류 AI 응답 >>>\n{}", aiResponse);

			// JSON 추출 및 파싱
			String jsonString = extractJsonString(aiResponse);
			if (!jsonString.isEmpty()) {
				JsonNode jsonNode = objectMapper.readTree(jsonString);
				
				// 각 정보 업데이트 (빈 값이 아닌 경우에만)
				updateInfoIfNotEmpty(collectedInfo, jsonNode, AiConstants.INFO_INTEREST);
				updateInfoIfNotEmpty(collectedInfo, jsonNode, AiConstants.INFO_LEVEL);
				updateInfoIfNotEmpty(collectedInfo, jsonNode, AiConstants.INFO_GOAL);
				updateInfoIfNotEmpty(collectedInfo, jsonNode, AiConstants.INFO_ADDITIONAL);
				
				log.info("[AiService] 정보 업데이트 완료: {}", collectedInfo);
			}
		} catch (Exception e) {
			log.warn("[AiService] AI 정보 분류 실패, 기존 정보 유지: {}", e.getMessage());
		}
	}

	private void updateInfoIfNotEmpty(Map<String, String> collectedInfo, JsonNode jsonNode, String key) {
		JsonNode valueNode = jsonNode.get(key);
		if (valueNode != null && !valueNode.asText().trim().isEmpty()) {
			String newValue = valueNode.asText().trim();
			
			// 기존 값이 있는 경우 추가 정보는 합치고, 다른 정보는 더 구체적인 것으로 업데이트
			if (AiConstants.INFO_ADDITIONAL.equals(key) && collectedInfo.containsKey(key)) {
				String existingValue = collectedInfo.get(key);
				collectedInfo.put(key, existingValue + " " + newValue);
			} else {
				collectedInfo.put(key, newValue);
			}
		}
	}

	private boolean isReadyForRecommendation(Map<String, String> collectedInfo) {
		// 최소 관심분야와 (목표 또는 난이도) 중 하나가 있으면 추천 가능
		return collectedInfo.containsKey(AiConstants.INFO_INTEREST) && 
			   (collectedInfo.containsKey(AiConstants.INFO_GOAL) && collectedInfo.containsKey(AiConstants.INFO_LEVEL));
	}

	private String formatCollectedInfo(Map<String, String> info) {
		StringBuilder formatted = new StringBuilder();
		info.forEach((key, value) -> {
			String keyName = switch (key) {
				case AiConstants.INFO_INTEREST -> AiConstants.LABEL_INTEREST;
				case AiConstants.INFO_LEVEL -> AiConstants.LABEL_LEVEL;
				case AiConstants.INFO_GOAL -> AiConstants.LABEL_GOAL;
				case AiConstants.INFO_ADDITIONAL -> AiConstants.LABEL_ADDITIONAL;
				default -> key;
			};
			formatted.append(keyName).append(": ").append(value).append("\n");
		});
		return formatted.toString();
	}

	public ChatMessageResponse getRecommendations(String promptText, boolean isFinalStep) {
		SystemMessage systemMessage = new SystemMessage(
			isFinalStep ? AiConstants.RECOMMENDATION_PROMPT : AiConstants.CASUAL_CONVERSATION_PROMPT
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
				.messageType(MessageType.CHAT)
				.build();
		}

		String pureJson = extractJsonString(rawAiResponse);

		if (pureJson.isEmpty()) {
			log.warn("[AiService] AI 응답에서 JSON을 찾을 수 없음.");
			return createErrorResponse(AiConstants.ERROR_AI_NO_RESPONSE);
		}

		try {
			JsonNode root = objectMapper.readTree(pureJson);
			JsonNode recNode = root.get("recommendations");

			if (recNode == null || !recNode.isArray()) {
				log.warn("[AiService] AI 응답에 'recommendations' 배열이 없거나 형식이 올바르지 않음.");
				return createErrorResponse(AiConstants.ERROR_AI_INVALID_FORMAT);
			}
			List<RecommendationDto> recommendations = objectMapper.convertValue(recNode,
				objectMapper.getTypeFactory().constructCollectionType(List.class, RecommendationDto.class));

			if (recommendations == null || recommendations.isEmpty()) {
				log.info("[AiService] AI가 추천할 강의를 찾지 못함.");
				return createErrorResponse(AiConstants.ERROR_NO_SUITABLE_LECTURES);
			}

			for (RecommendationDto rec : recommendations) {
				if (rec.lectureId() == null) {
					log.warn("[AiService] 추천 결과에 lectureId가 없음: {}", rec.title());
				} else {
					log.info("[AiService] 추천 강의 검증 성공: lectureId={}, title={}", rec.lectureId(), rec.title());
				}
			}

			log.info("[AiService] AI 추천 결과 파싱 성공: {}", recommendations);
			return ChatMessageResponse.builder()
				.message(null)
				.recommendations(recommendations)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

		} catch (JsonProcessingException e) {
			log.error("[AiService] AI 응답 파싱 실패!\n원본: {}\n추출된 JSON: {}\n에러: {}\n{}",
				rawAiResponse, pureJson, e.toString(), e.getMessage());
			return createErrorResponse(AiConstants.ERROR_AI_PARSING_FAILED);
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
			.messageType(MessageType.ERROR)
			.build();
	}
}