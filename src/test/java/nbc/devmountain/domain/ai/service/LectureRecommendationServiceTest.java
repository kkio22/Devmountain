package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.repository.ChatRoomRepository;
import nbc.devmountain.domain.chat.service.ChatRoomService;
import nbc.devmountain.domain.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.recommendation.dto.RecommendationDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.search.dto.BraveSearchResponseDto;
import nbc.devmountain.domain.search.sevice.BraveSearchService;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureRecommendationService 단위 테스트")
class LectureRecommendationServiceTest {

	@Mock
	private RagService ragService;
	@Mock
	private AiService aiService;
	@Mock
	private BraveSearchService braveSearchService;
	@Mock
	private CacheService cacheService;
	@Mock
	private ChatRoomService chatRoomService;
	@Mock
	private ChatRoomRepository chatRoomRepository;

	private LectureRecommendationService lectureRecommendationService;

	private Long chatRoomId;
	private User.MembershipLevel memberType;
	private WebSocketSession session;

	@BeforeEach
	void setUp() {
		chatRoomId = 1L;
		memberType = User.MembershipLevel.PRO;
		session = mock(WebSocketSession.class);

		lectureRecommendationService = new LectureRecommendationService(ragService, aiService, braveSearchService,
			cacheService, chatRoomService, chatRoomRepository);
	}

	@Nested
	@DisplayName("recommendationResponse 메서드 테스트")
	class RecommendationResponseTest {

		@Test
		@DisplayName("빈 쿼리 입력 시 에러 응답 반환")
		void shouldReturnErrorWhenQueryIsEmpty() {
			String emptyQuery = "";

			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(emptyQuery, memberType,
				chatRoomId, session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_EMPTY_QUERY);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("null 쿼리 입력 시 에러 응답 반환")
		void shouldReturnErrorWhenQueryIsNull() {
			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(null, memberType,
				chatRoomId, session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_EMPTY_QUERY);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("chatRoomId가 null인 경우 에러 응답 반환")
		void shouldReturnErrorWhenChatRoomIdIsNull() {
			String query = "자바 배우고 싶어요";

			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(query, memberType, null,
				session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_CHATROOM);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("처리 중 예외 발생 시 에러 응답 반환")
		void shouldReturnErrorWhenExceptionOccurs() {
			String query = "자바 배우고 싶어요";
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenThrow(
				new RuntimeException("AI 서비스 에러"));

			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(query, memberType,
				chatRoomId, session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_PROCESSING_FAILED);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}
	}

	@Nested
	@DisplayName("첫 대화 처리 테스트")
	class FirstConversationTest {

		@Test
		@DisplayName("첫 번째 메시지에 대해 AI 대화 분석 수행")
		void shouldAnalyzeFirstMessage() {
			String query = "자바 스프링 배우고 싶어요";
			ChatMessageResponse mockResponse = ChatMessageResponse.builder()
				.message("자바와 스프링에 관심이 있으시군요! 어떤 목적으로 학습하고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(mockResponse);

			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(query, memberType,
				chatRoomId, session);

			verify(aiService).analyzeConversationAndDecideNext(eq("사용자: " + query + "\n"), anyMap(), eq(query),
				eq(memberType), any(WebSocketSession.class), eq(chatRoomId));
			assertThat(response.getMessage()).contains("자바와 스프링");
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}
	}

	@Nested
	@DisplayName("대화 진행 테스트")
	class ConversationProgressTest {

		@Test
		@DisplayName("대화 진행 중 AI가 추가 질문 생성")
		void shouldContinueConversationWithAIQuestions() {
			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업하려고요";

			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			ChatMessageResponse secondResponse = ChatMessageResponse.builder()
				.message("취업 목적이시군요! 현재 프로그래밍 경험은 어느 정도인가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(firstQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(secondResponse);

			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, session);
			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType,
				chatRoomId, session);

			verify(aiService, times(2)).analyzeConversationAndDecideNext(anyString(), anyMap(), anyString(),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong());
			assertThat(response.getMessage()).contains("프로그래밍 경험");
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}

		@Test
		@DisplayName("충분한 정보 수집 후 추천 단계로 진행")
		void shouldProceedToRecommendationWhenInfoCollected() {
			setupForChatRoomUpdate();

			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업 준비용으로요";
			List<Lecture> mockLectures = List.of(createMockLecture());
			List<RecommendationDto> mockRecommendations = List.of(createMockRecommendation());

			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			ChatMessageResponse readyResponse = ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();
			ChatMessageResponse finalResponse = ChatMessageResponse.builder()
				.recommendations(mockRecommendations)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(firstQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString())).thenReturn(mockLectures);
			when(braveSearchService.search(anyString())).thenReturn(mockBraveSearchResponse());
			when(aiService.getRecommendations(anyString(), eq(true), eq(memberType))).thenReturn(finalResponse);

			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, session);
			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType,
				chatRoomId, session);

			verify(ragService, times(1)).searchSimilarLectures(anyString());
			verify(aiService, times(1)).getRecommendations(anyString(), eq(true), eq(memberType));
			assertThat(response.getRecommendations()).isNotNull();
			assertThat(response.getRecommendations()).hasSize(1);
			assertThat(response.getMessageType()).isEqualTo(MessageType.RECOMMENDATION);
		}
	}

	@Nested
	@DisplayName("추천 생성 테스트")
	class RecommendationGenerationTest {

		@Test
		@DisplayName("강의 검색 결과가 없는 경우 에러 응답")
		void shouldReturnErrorWhenNoLecturesFound() {
			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업 준비용으로요";

			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			ChatMessageResponse readyResponse = ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(firstQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString())).thenReturn(Collections.emptyList());

			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, session);
			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType,
				chatRoomId, session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_LECTURES_FOUND);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
			assertThat(response.isAiResponse()).isTrue();
		}

		@Test
		@DisplayName("RAG 검색 중 예외 발생 시 에러 응답")
		void shouldReturnErrorWhenRagSearchFails() {
			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업 준비용으로요";

			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			ChatMessageResponse readyResponse = ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(firstQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString())).thenThrow(new RuntimeException("RAG 검색 실패"));

			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, session);
			ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType,
				chatRoomId, session);

			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_LECTURE_SEARCH_FAILED);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
			assertThat(response.isAiResponse()).isTrue();
		}
	}

	@Nested
	@DisplayName("상태 관리 테스트")
	class StateManagementTest {

		@Test
		@DisplayName("여러 채팅방의 대화 히스토리가 독립적으로 관리됨")
		void shouldManageConversationHistoryIndependently() {
			Long chatRoom1 = 1L;
			Long chatRoom2 = 2L;
			String query1 = "자바 배우고 싶어요";
			String query2 = "파이썬 배우고 싶어요";

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), anyString(),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(
				ChatMessageResponse.builder().message("응답").messageType(MessageType.CHAT).build());

			lectureRecommendationService.recommendationResponse(query1, memberType, chatRoom1, session);
			lectureRecommendationService.recommendationResponse(query2, memberType, chatRoom2, session);

			verify(aiService).analyzeConversationAndDecideNext(eq("사용자: " + query1 + "\n"), anyMap(), eq(query1),
				eq(memberType), any(WebSocketSession.class), eq(chatRoom1));
			verify(aiService).analyzeConversationAndDecideNext(eq("사용자: " + query2 + "\n"), anyMap(), eq(query2),
				eq(memberType), any(WebSocketSession.class), eq(chatRoom2));
		}
	}

	@Test
	@DisplayName("유사 질문이 캐시에 존재할 경우 RAG 호출 없이 응답 반환")
	void shouldReturnCachedLecturesWhenCacheHit() {
		setupForChatRoomUpdate();

		String firstQuery = "자바 배우고 싶어요";
		String secondQuery = "취업 준비입니다.";

		ChatMessageResponse firstResponse = ChatMessageResponse.builder()
			.message("어떤 목적으로 자바를 배우고 싶으신가요?")
			.isAiResponse(true)
			.messageType(MessageType.CHAT)
			.build();
		ChatMessageResponse readyResponse = ChatMessageResponse.builder()
			.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
			.isAiResponse(true)
			.messageType(MessageType.RECOMMENDATION)
			.build();
		List<Lecture> cachedLectures = List.of(createMockLecture());
		ChatMessageResponse finalResponse = ChatMessageResponse.builder()
			.recommendations(List.of(createMockRecommendation()))
			.isAiResponse(true)
			.messageType(MessageType.RECOMMENDATION)
			.build();

		when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(firstQuery),
			any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(firstResponse);
		when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
			any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong())).thenReturn(readyResponse);
		when(cacheService.search(anyString())).thenReturn(cachedLectures);
		when(aiService.getRecommendations(anyString(), eq(true), eq(User.MembershipLevel.PRO))).thenReturn(
			finalResponse);
		when(braveSearchService.search(anyString())).thenReturn(mockBraveSearchResponse());

		lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, session);
		ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType,
			chatRoomId, session);

		verify(cacheService, times(1)).search(anyString());
		verify(ragService, never()).searchSimilarLectures(anyString());
		assertThat(response.getRecommendations()).isNotNull();
	}

	private void setupForChatRoomUpdate() {
		ChatRoom mockChatRoom = mock(ChatRoom.class);
		User mockUser = mock(User.class);

		when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(mockChatRoom));
		when(mockChatRoom.getChatroomName()).thenReturn("새 채팅방");
		when(mockChatRoom.getUser()).thenReturn(mockUser);
		when(mockUser.getUserId()).thenReturn(1L);
		when(aiService.summarizeChatRoomName(anyString())).thenReturn("요약된 채팅방 이름");
	}

	private Lecture createMockLecture() {
		return Lecture.builder()
			.title("스프링 입문 강의")
			.description("스프링 프레임워크 기초 학습")
			.instructor("김강사")
			.levelCode("초급")
			.thumbnailUrl("thumbnail.jpg")
			.isFree(false)
			.payPrice(new java.math.BigDecimal("15000"))
			.build();
	}

	private RecommendationDto createMockRecommendation() {
		return new RecommendationDto(1L, "thumbnail.jpg", "스프링 입문 강의", "스프링 프레임워크 기초 학습", "김강사", "초급",
			"https://www.example.com/course/", "15000", "false", "VECTOR",0.5f);
	}

	private BraveSearchResponseDto mockBraveSearchResponse() {
		BraveSearchResponseDto.Result.ThumbnailWrapper thumbnail = new BraveSearchResponseDto.Result.ThumbnailWrapper(
			"https://imgs.search.brave.com/thumb.jpg", "https://example.com/original.jpg");
		BraveSearchResponseDto.Result result = new BraveSearchResponseDto.Result("AI 기초 강의", "AI 입문자를 위한 강의입니다.",
			"http://example.com/ai-course", thumbnail);
		BraveSearchResponseDto.Web web = new BraveSearchResponseDto.Web(List.of(result));
		return new BraveSearchResponseDto(web);
	}
}