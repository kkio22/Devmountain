package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.user.model.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService 단위 테스트")
class AiServiceTest {

	@Mock
	private ChatModel chatModel;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private AiService aiService;

	private Map<String, String> collectedInfo;
	private String conversationHistory;
	private String userMessage;

	@BeforeEach
	void setUp() {
		collectedInfo = new HashMap<>();
		conversationHistory = "사용자: 자바 배우고 싶어요\n";
		userMessage = "취업하려고요";
	}

	@Nested
	@DisplayName("analyzeConversationAndDecideNext 메서드 테스트")
	class AnalyzeConversationTest {

		@Test
		@DisplayName("대화 분석 후 추가 질문 생성")
		void shouldGenerateFollowUpQuestion() {
			// given
			String aiResponse = "취업 목적이시군요! 현재 프로그래밍 경험은 어느 정도인가요?";
			ChatResponse mockChatResponse = createMockChatResponse(aiResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			verify(chatModel, atLeastOnce()).call(any(Prompt.class));
			assertThat(response.getMessage()).isEqualTo(aiResponse);
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
			assertThat(response.isAiResponse()).isTrue();
		}

		@Test
		@DisplayName("충분한 정보 수집 시 추천 준비 완료 응답")
		void shouldReturnReadyForRecommendationWhenInfoComplete() {
			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바 스프링");
			collectedInfo.put(AiConstants.INFO_GOAL, "취업");

			String aiResponse = AiConstants.READY_FOR_RECOMMENDATION;
			ChatResponse mockChatResponse = createMockChatResponse(aiResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION);
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}

		@Test
		@DisplayName("정보 추출 실패 시에도 대화 계속 진행")
		void shouldContinueConversationWhenInfoExtractionFails() throws Exception {
			// given
			String aiResponse = "어떤 분야에 관심이 있으신가요?";
			ChatResponse mockChatResponse = createMockChatResponse(aiResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(aiResponse);
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}
	}

	@Nested
	@DisplayName("getRecommendations 메서드 테스트")
	class GetRecommendationsTest {

		@Test
		@DisplayName("최종 추천 단계에서 JSON 형태 추천 결과 반환")
		void shouldReturnRecommendationsInJsonFormat() throws Exception {
			// given
			String promptText = "추천 요청";
			String jsonResponse = """
				{
					"recommendations": [
						{
							"title": "자바 기초 강의",
							"description": "자바 기초부터 차근차근"
						}
					]
				}
				""";

			// Real ObjectMapper를 사용하여 실제 JSON 파싱 테스트
			AiService realAiService = new AiService(chatModel, new ObjectMapper());
			ChatResponse mockChatResponse = createMockChatResponse(jsonResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = realAiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getRecommendations()).isNotNull();
			assertThat(response.getRecommendations()).hasSize(1);
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
			assertThat(response.getMessage()).isNull();
		}

		@Test
		@DisplayName("일반 대화 단계에서 텍스트 응답 반환")
		void shouldReturnTextResponseForCasualConversation() {
			// given
			String promptText = "대화 요청";
			String textResponse = "자연스러운 대화 응답입니다.";
			ChatResponse mockChatResponse = createMockChatResponse(textResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.getRecommendations(promptText, false, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(textResponse);
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
			assertThat(response.getRecommendations()).isNull();
		}

		@Test
		@DisplayName("AI 응답에서 JSON을 찾을 수 없는 경우 에러 응답")
		void shouldReturnErrorWhenJsonNotFound() {
			// given
			String promptText = "추천 요청";
			String nonJsonResponse = "JSON이 아닌 일반 텍스트 응답";
			ChatResponse mockChatResponse = createMockChatResponse(nonJsonResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_AI_INVALID_FORMAT);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("JSON 파싱 실패 시 에러 응답")
		void shouldReturnErrorWhenJsonParsingFails() throws Exception {
			// given
			String promptText = "추천 요청";
			String invalidJsonResponse = "{ invalid json }";
			ChatResponse mockChatResponse = createMockChatResponse(invalidJsonResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
			when(objectMapper.readTree(anyString())).thenThrow(new JsonProcessingException("JSON 파싱 실패") {
			});

			// when
			ChatMessageResponse response = aiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_AI_PARSING_FAILED);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("추천 결과가 비어있는 경우 에러 응답")
		void shouldReturnErrorWhenRecommendationsEmpty() throws Exception {
			// given
			String promptText = "추천 요청";
			String emptyJsonResponse = """
				{
					"recommendations": []
				}
				""";

			// Real ObjectMapper를 사용하여 실제 JSON 파싱 테스트
			AiService realAiService = new AiService(chatModel, new ObjectMapper());
			ChatResponse mockChatResponse = createMockChatResponse(emptyJsonResponse);

			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = realAiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_SUITABLE_LECTURES);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}
	}

	@Nested
	@DisplayName("정보 추출 및 업데이트 테스트")
	class InfoExtractionTest {

		@Test
		@DisplayName("AI가 사용자 메시지에서 관심분야 정보 추출")
		void shouldExtractInterestInfo() throws Exception {
			// given
			String extractionResponse = """
				{
					"interest": "자바 스프링 백엔드",
					"level": "",
					"goal": "",
					"additional": ""
				}
				""";

			ChatResponse mockChatResponse = createMockChatResponse(extractionResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
			when(objectMapper.readTree(anyString())).thenReturn(
				new ObjectMapper().readTree(extractionResponse));

			// when
			aiService.analyzeConversationAndDecideNext(conversationHistory, collectedInfo, "자바 스프링 백엔드 배우고 싶어요", User.MembershipLevel.GUEST);

			// then
			verify(chatModel, atLeast(2)).call(any(Prompt.class)); // 대화 분석 + 정보 추출
		}

		@Test
		@DisplayName("기존 정보와 새로운 정보 병합")
		void shouldMergeExistingAndNewInfo() throws Exception {
			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바");
			collectedInfo.put(AiConstants.INFO_ADDITIONAL, "온라인 강의");

			String extractionResponse = """
				{
					"interest": "자바 스프링",
					"level": "초급",
					"goal": "",
					"additional": "실습 위주"
				}
				""";

			ChatResponse mockChatResponse = createMockChatResponse(extractionResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
			when(objectMapper.readTree(anyString())).thenReturn(
				new ObjectMapper().readTree(extractionResponse));

			// when
			aiService.analyzeConversationAndDecideNext(conversationHistory, collectedInfo, userMessage,
				User.MembershipLevel.GUEST);

			// then
			// 정보가 업데이트되었는지 간접적으로 확인 (Mock 호출 검증)
			verify(objectMapper, atLeastOnce()).readTree(anyString());
		}
	}

	@Nested
	@DisplayName("추천 준비 완료 판단 테스트")
	class ReadyForRecommendationTest {

		@Test
		@DisplayName("관심분야와 목표가 있으면 추천 준비 완료")
		void shouldBeReadyWithInterestAndGoal() {
			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바");
			collectedInfo.put(AiConstants.INFO_GOAL, "취업");
			collectedInfo.put(AiConstants.INFO_LEVEL, "고급");

			String readyResponse = "일반 응답";
			ChatResponse mockChatResponse = createMockChatResponse(readyResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			// isReadyForRecommendation이 true를 반환하므로 RECOMMENDATION 메시지 타입이 되어야 함
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}

		@Test
		@DisplayName("관심분야와 난이도가 있으면 추천 준비 완료")
		void shouldBeReadyWithInterestAndLevel() {
			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바");
			collectedInfo.put(AiConstants.INFO_GOAL, "취업");
			collectedInfo.put(AiConstants.INFO_LEVEL, "초급");

			String readyResponse = "일반 응답";
			ChatResponse mockChatResponse = createMockChatResponse(readyResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}

		@Test
		@DisplayName("관심분야만 있으면 추천 준비 미완료")
		void shouldNotBeReadyWithOnlyInterest() {
			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바");

			String continueResponse = "더 자세한 정보를 알려주세요";
			ChatResponse mockChatResponse = createMockChatResponse(continueResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST);

			// then
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}
	}

	private ChatResponse createMockChatResponse(String content) {
		Generation generation = new Generation(new AssistantMessage(content));
		return new ChatResponse(List.of(generation));
	}

	private RecommendationDto createMockRecommendationDto() {
		return new RecommendationDto(
			1L,
			"thumb1.jpg",
			"자바 기초 강의",
			"자바 기초부터 차근차근",
			"김강사",
			"초급",
			"https://www.example.com/course/",
			"15000원",
			"false"
		);
	}
} 
