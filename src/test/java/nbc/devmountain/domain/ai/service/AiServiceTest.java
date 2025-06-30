package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nbc.devmountain.common.monitering.CustomMetrics;
import nbc.devmountain.domain.chat.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.user.model.User;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiService 단위 테스트")
class AiServiceTest {

	@Mock
	private ChatModel chatModel;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private OpenAiChatModel streamingChatModel;

	@Mock
	private ChatMessageService chatMessageService;

	@Mock
	private CustomMetrics customMetrics;

	@InjectMocks
	private AiService aiService;

	private Map<String, String> collectedInfo;
	private String conversationHistory;
	private String userMessage;
	private WebSocketSession session;
	private Long chatRoomId;

	@BeforeEach
	void setUp() throws Exception {
		collectedInfo = new HashMap<>();
		conversationHistory = "사용자: 자바 배우고 싶어요\n";
		userMessage = "취업하려고요";
		session = mock(WebSocketSession.class);

		doNothing().when(session).sendMessage(any(TextMessage.class));
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"test\"}");

		chatRoomId = 1L;
	}

	@Test
	@DisplayName("일반 대화 단계에서 텍스트 응답 반환")
	void shouldReturnTextResponseForCasualConversation() {
		// given
		ChatModel testChatModel = mock(ChatModel.class);
		AiService testAiService = new AiService(testChatModel, new ObjectMapper(), streamingChatModel, chatMessageService, customMetrics);

		String promptText = "대화 요청";
		String textResponse = "자연스러운 대화 응답입니다.";
		ChatResponse mockChatResponse = createMockChatResponse(textResponse);

		when(testChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

		// when
		ChatMessageResponse response = testAiService.getRecommendations(promptText, false, User.MembershipLevel.GUEST);

		// then
		assertThat(response.getMessage()).isEqualTo(textResponse);
		assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		assertThat(response.getRecommendations()).isNull();
	}

	@Test
	@DisplayName("AI 응답에서 JSON을 찾을 수 없는 경우 에러 응답")
	void shouldReturnErrorWhenJsonNotFound() {
		// given
		ChatModel testChatModel = mock(ChatModel.class);
		AiService testAiService = new AiService(testChatModel, new ObjectMapper(), streamingChatModel, chatMessageService, customMetrics);

		String promptText = "추천 요청";
		String nonJsonResponse = "JSON이 아닌 일반 텍스트 응답";
		ChatResponse mockChatResponse = createMockChatResponse(nonJsonResponse);

		when(testChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

		// when
		ChatMessageResponse response = testAiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

		// then
		assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_AI_INVALID_FORMAT);
		assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
	}

	@Test
	@DisplayName("최종 추천 단계에서 JSON 형태 추천 결과 반환")
	void shouldReturnRecommendationsInJsonFormat() throws Exception {
		// given
		String promptText = "추천 요청";
		String jsonResponse = """
         {
            "recommendations": [
               {
                  "lectureId": 1,
                  "thumbnailUrl": "thumb1.jpg",
                  "title": "자바 기초 강의",
                  "description": "자바 기초부터 차근차근",
                  "instructor": "김강사",
                  "level": "초급",
                  "url": "https://www.example.com/course/",
                  "payPrice": "15000원",
                  "isFree": "false"
               }
            ]
         }
         """;

		// 실제 ObjectMapper 사용
		AiService realAiService = new AiService(chatModel, new ObjectMapper(), streamingChatModel, chatMessageService, customMetrics);
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
	@DisplayName("추천 결과가 비어있는 경우 에러 응답")
	void shouldReturnErrorWhenRecommendationsEmpty() throws Exception {
		// given
		String promptText = "추천 요청";
		String emptyJsonResponse = """
         {
            "recommendations": []
         }
         """;

		AiService realAiService = new AiService(chatModel, new ObjectMapper(), streamingChatModel, chatMessageService, customMetrics);
		ChatResponse mockChatResponse = createMockChatResponse(emptyJsonResponse);

		when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

		// when
		ChatMessageResponse response = realAiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

		// then
		assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_SUITABLE_LECTURES);
		assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
	}

	@Test
	@DisplayName("JSON 파싱 실패 시 에러 응답")
	void shouldReturnErrorWhenJsonParsingFails() throws Exception {
		// given
		ChatModel testChatModel = mock(ChatModel.class);
		ObjectMapper testObjectMapper = mock(ObjectMapper.class);
		AiService testAiService = new AiService(testChatModel, testObjectMapper, streamingChatModel, chatMessageService, customMetrics);

		String promptText = "추천 요청";
		String invalidJsonResponse = "{ invalid json }";
		ChatResponse mockChatResponse = createMockChatResponse(invalidJsonResponse);

		when(testChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
		when(testObjectMapper.readTree(anyString())).thenThrow(new JsonProcessingException("JSON 파싱 실패") {});

		// when
		ChatMessageResponse response = testAiService.getRecommendations(promptText, true, User.MembershipLevel.GUEST);

		// then
		assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_AI_PARSING_FAILED);
		assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
	}

	@Test
	@DisplayName("충분한 정보 수집 시 추천 준비 완료 응답")
	void shouldReturnReadyForRecommendationWhenInfoComplete() {
		// given - 충분한 정보가 이미 수집된 상태
		collectedInfo.put(AiConstants.INFO_INTEREST, "자바 스프링");
		collectedInfo.put(AiConstants.INFO_GOAL, "취업");
		collectedInfo.put(AiConstants.INFO_LEVEL, "초급");

		// when
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response.getMessage()).isEqualTo(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION);
		assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
	}

	@Test
	@DisplayName("정보 부족 시 대화 계속 진행")
	void shouldContinueConversationWhenInfoInsufficient() {
		// given
		ChatModel testChatModel = mock(ChatModel.class);
		OpenAiChatModel testStreamingChatModel = mock(OpenAiChatModel.class);
		AiService testAiService = new AiService(testChatModel, new ObjectMapper(), testStreamingChatModel, chatMessageService, customMetrics);

		collectedInfo.put(AiConstants.INFO_INTEREST, "자바");

		String continueResponse = "더 자세한 정보를 알려주세요";
		ChatResponse streamingResponse = createMockChatResponse(continueResponse);

		// 정보 추출 실패 (null 반환)
		when(testChatModel.call(any(Prompt.class))).thenReturn(null);
		when(testStreamingChatModel.stream(any(Prompt.class)))
			.thenReturn(Flux.just(streamingResponse));

		// when
		ChatMessageResponse response = testAiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		assertThat(response.getMessage()).isEqualTo(continueResponse);
	}

	@Test
	@DisplayName("관심분야와 목표가 있으면 추천 준비 완료")
	void shouldBeReadyWithInterestAndGoal() {
		// given
		collectedInfo.put(AiConstants.INFO_INTEREST, "자바");
		collectedInfo.put(AiConstants.INFO_GOAL, "취업");
		collectedInfo.put(AiConstants.INFO_LEVEL, "고급");

		// when
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
	}

	@Test
	@DisplayName("관심분야와 난이도가 있으면 추천 준비 완료")
	void shouldBeReadyWithInterestAndLevel() {
		// given
		collectedInfo.put(AiConstants.INFO_INTEREST, "자바");
		collectedInfo.put(AiConstants.INFO_GOAL, "취업");
		collectedInfo.put(AiConstants.INFO_LEVEL, "초급");

		// when
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
	}

	@Test
	@DisplayName("정보 추출 성공 시나리오")
	void shouldExtractInfoSuccessfully() throws Exception {
		// given
		String extractionResponse = """
         {
            "interest": "자바 스프링 백엔드",
            "level": "",
            "goal": "",
            "additional": ""
         }
         """;

		ChatResponse infoExtractionResponse = createMockChatResponse(extractionResponse);
		ChatResponse streamingResponse = createMockChatResponse("관심분야를 업데이트했습니다.");

		when(chatModel.call(any(Prompt.class))).thenReturn(infoExtractionResponse);
		when(streamingChatModel.stream(any(Prompt.class)))
			.thenReturn(Flux.just(streamingResponse));
		when(objectMapper.readTree(anyString())).thenReturn(
			new ObjectMapper().readTree(extractionResponse));

		// when
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, "자바 스프링 백엔드 배우고 싶어요",
			User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
	}

	@Test
	@DisplayName("정보 병합 테스트")
	void shouldMergeInfoCorrectly() throws Exception {
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

		ChatResponse infoExtractionResponse = createMockChatResponse(extractionResponse);
		ChatResponse streamingResponse = createMockChatResponse("정보를 업데이트했습니다.");

		when(chatModel.call(any(Prompt.class))).thenReturn(infoExtractionResponse);
		when(streamingChatModel.stream(any(Prompt.class)))
			.thenReturn(Flux.just(streamingResponse));
		when(objectMapper.readTree(anyString())).thenReturn(
			new ObjectMapper().readTree(extractionResponse));

		// when
		ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
			conversationHistory, collectedInfo, userMessage,
			User.MembershipLevel.GUEST, session, chatRoomId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
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
			"false",
			"VECTOR",
			0.5F
		);
	}
}
