package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.chat.service.ChatMessageService;
import nbc.devmountain.domain.user.model.User;
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
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService 단위 테스트")
class AiServiceTest {

	@Mock
	private ChatModel chatModel;

	@Mock
	private StreamingChatModel streamingChatModel;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ChatMessageService chatMessageService;

	@Mock
	private WebSocketSession webSocketSession;

	@InjectMocks
	private AiService aiService;

	private Map<String, String> collectedInfo;
	private String conversationHistory;
	private String userMessage;
	private Long chatRoomId;

	private final ObjectMapper realObjectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() throws IOException {
		collectedInfo = new HashMap<>();
		conversationHistory = "사용자: 자바 배우고 싶어요\n";
		userMessage = "취업하려고요";
		chatRoomId = 1L;

		lenient().when(webSocketSession.isOpen()).thenReturn(true);
		lenient().doNothing().when(webSocketSession).sendMessage(any(TextMessage.class));

		// getTypeFactory() 호출을 실제 객체에 위임
		lenient().when(objectMapper.getTypeFactory()).thenReturn(realObjectMapper.getTypeFactory());

		// readTree(String) 호출을 실제 객체에 위임
		lenient().when(objectMapper.readTree(anyString())).thenAnswer(
			invocation -> realObjectMapper.readTree(invocation.getArgument(0, String.class))
		);

		// convertValue 호출을 실제 객체에 위임
		lenient().when(objectMapper.convertValue(any(), isA(TypeReference.class))).thenAnswer(
			invocation -> realObjectMapper.convertValue(invocation.getArgument(0), invocation.getArgument(1, TypeReference.class))
		);
		lenient().when(objectMapper.convertValue(any(), isA(JavaType.class))).thenAnswer(
			invocation -> realObjectMapper.convertValue(invocation.getArgument(0), invocation.getArgument(1, JavaType.class))
		);
	}

	@Nested
	@DisplayName("analyzeConversationAndDecideNext 메서드 테스트")
	class AnalyzeConversationTest {

		@Test
		@DisplayName("대화 분석 후 추가 질문 생성 (스트리밍 사용)")
		void shouldGenerateFollowUpQuestion() {

			// given
			String expectedResponse = "취업 목적이시군요! 현재 프로그래밍 경험은 어느 정도인가요?";

			when(chatModel.call(any(Prompt.class)))
				.thenReturn(createMockChatResponse("{\"goal\":\"취업\"}"));
			when(streamingChatModel.stream(any(Prompt.class)))
				.thenReturn(Flux.just(createMockChatResponse(expectedResponse)));

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, webSocketSession, chatRoomId);

			// then
			verify(streamingChatModel).stream(any(Prompt.class));

			assertThat(response.getMessage()).isEqualTo(expectedResponse);
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}

		@Test
		@DisplayName("충분한 정보 수집 시 추천 준비 완료 응답 (스트리밍 미사용)")
		void shouldReturnReadyForRecommendationWhenInfoComplete() {

			// given
			collectedInfo.put(AiConstants.INFO_INTEREST, "자바 스프링");
			collectedInfo.put(AiConstants.INFO_GOAL, "취업");
			collectedInfo.put(AiConstants.INFO_LEVEL, "초급");

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, webSocketSession, chatRoomId);

			// then
			verify(streamingChatModel, never()).stream(any(Prompt.class));

			assertThat(response.getMessage()).isEqualTo(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION);
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}

		@Test
		@DisplayName("정보 추출 실패 시에도 대화 계속 진행 (스트리밍 사용)")
		void shouldContinueConversationWhenInfoExtractionFails() {

			// given
			String expectedResponse = "어떤 분야에 관심이 있으신가요?";

			when(chatModel.call(any(Prompt.class)))
				.thenReturn(createMockChatResponse("이건 JSON이 아니에요"));
			when(streamingChatModel.stream(any(Prompt.class)))
				.thenReturn(Flux.just(createMockChatResponse(expectedResponse)));

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, webSocketSession, chatRoomId);

			// then
			verify(streamingChatModel).stream(any(Prompt.class));

			assertThat(response.getMessage()).isEqualTo(expectedResponse);
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
			ChatResponse mockChatResponse = createMockChatResponse(jsonResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);



			//when
			ChatMessageResponse response = aiService.getRecommendations(promptText, true);

			// then
			assertThat(response.getRecommendations()).isNotNull();
			assertThat(response.getRecommendations()).hasSize(1);
			assertThat(response.getRecommendations().get(0).title()).isEqualTo("자바 기초 강의");
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
			ChatMessageResponse response = aiService.getRecommendations(promptText, false);

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
			ChatMessageResponse response = aiService.getRecommendations(promptText, true);

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

			// when
			ChatMessageResponse response = aiService.getRecommendations(promptText, true);

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

			ChatResponse mockChatResponse = createMockChatResponse(emptyJsonResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

			//when
			ChatMessageResponse response = aiService.getRecommendations(promptText, true);

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
                "interest": "자바 스프링 백엔드"
             }
             """;

			ChatResponse mockChatResponse = createMockChatResponse(extractionResponse);
			when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
			when(streamingChatModel.stream(any(Prompt.class))).thenReturn(Flux.just(createMockChatResponse("알겠습니다. 자바 스프링 백엔드에 관심 있으시군요.")));

			// when
			aiService.analyzeConversationAndDecideNext(conversationHistory, collectedInfo,
				"자바 스프링 백엔드 배우고 싶어요", User.MembershipLevel.GUEST, webSocketSession, chatRoomId);

			// then
			verify(chatModel, atLeastOnce()).call(any(Prompt.class));
			verify(streamingChatModel, atLeastOnce()).stream(any(Prompt.class));
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

			String infoExtractionResponse = "{\"goal\":\"취업\"}";
			when(chatModel.call(any(Prompt.class))).thenReturn(createMockChatResponse(infoExtractionResponse));
			lenient().when(streamingChatModel.stream(any(Prompt.class))).thenReturn(Flux.just());

			// when
			ChatMessageResponse response = aiService.analyzeConversationAndDecideNext(
				conversationHistory, collectedInfo, userMessage, User.MembershipLevel.GUEST, webSocketSession, chatRoomId);

			// then
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}
	}

	private ChatResponse createMockChatResponse(String content) {
		Generation generation = new Generation(new AssistantMessage(content));
		return new ChatResponse(List.of(generation));
	}
}