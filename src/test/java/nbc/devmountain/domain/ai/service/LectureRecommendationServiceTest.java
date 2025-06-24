package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.chat.repository.ChatRoomRepository;
import nbc.devmountain.domain.chat.service.ChatRoomService;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.search.dto.BraveSearchResponseDto;
import nbc.devmountain.domain.search.sevice.BraveSearchService;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.chat.RoomType;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureRecommendationService 단위 테스트")
class LectureRecommendationServiceTest {

	@Mock
	private RagService ragService;

	@Mock
	private AiService aiService;

	@Mock
	private CacheService cacheService;

	@Mock
	private BraveSearchService braveSearchService;

	@Mock
	private ChatRoomService chatRoomService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private WebSocketSession webSocketSession;

	@InjectMocks
	private LectureRecommendationService lectureRecommendationService;

	private Long chatRoomId;
	private User.MembershipLevel memberType;

	@BeforeEach
	void setUp() {
		chatRoomId = 1L;
		memberType = User.MembershipLevel.PRO;
	}

	@Nested
	@DisplayName("recommendationResponse 메서드 테스트")
	class RecommendationResponseTest {

		@Test
		@DisplayName("빈 쿼리 입력 시 에러 응답 반환")
		void shouldReturnErrorWhenQueryIsEmpty() {
			// given
			String emptyQuery = "";

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(emptyQuery, memberType, chatRoomId, webSocketSession);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_EMPTY_QUERY);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
			assertThat(response.isAiResponse()).isTrue();
		}

		@Test
		@DisplayName("null 쿼리 입력 시 에러 응답 반환")
		void shouldReturnErrorWhenQueryIsNull() {
			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(null, memberType, chatRoomId, webSocketSession);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_EMPTY_QUERY);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("chatRoomId가 null인 경우 에러 응답 반환")
		void shouldReturnErrorWhenChatRoomIdIsNull() {
			// given
			String query = "자바 배우고 싶어요";

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(query, memberType, null, webSocketSession);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_CHATROOM);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("처리 중 예외 발생 시 에러 응답 반환")
		void shouldReturnErrorWhenExceptionOccurs() {
			// given
			String query = "자바 배우고 싶어요";
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenThrow(new RuntimeException("AI 서비스 에러"));

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(query, memberType, chatRoomId, webSocketSession);

			// then
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
			// given
			String query = "자바 스프링 배우고 싶어요";
			ChatMessageResponse mockResponse = ChatMessageResponse.builder()
				.message("자바와 스프링에 관심이 있으시군요! 어떤 목적으로 학습하고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(mockResponse);

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(query, memberType, chatRoomId, webSocketSession);

			// then
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query + "\n"),
				anyMap(),
				eq(query),
				eq(memberType),
				eq(webSocketSession),
				eq(chatRoomId)
			);
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
			// given
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
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(secondResponse);

			//when
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, webSocketSession);
			ChatMessageResponse finalResponse = lectureRecommendationService.recommendationResponse(secondQuery,
				memberType, chatRoomId, webSocketSession);

			//then
			verify(aiService, times(2)).analyzeConversationAndDecideNext(anyString(), anyMap(), anyString(),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong());
			assertThat(finalResponse.getMessage()).isEqualTo("취업 목적이시군요! 현재 프로그래밍 경험은 어느 정도인가요?");
			assertThat(finalResponse.getMessageType()).isEqualTo(MessageType.CHAT);
		}
	}

	@Nested
	@DisplayName("추천 생성 테스트")
	class RecommendationGenerationTest {
		@Test
		@DisplayName("강의 검색 결과가 없는 경우 에러 응답")
		void shouldReturnErrorWhenNoLecturesFound() {
			// given
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
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(readyResponse);
			when(cacheService.cacheSimilarLectures(anyString())).thenReturn(null);
			when(ragService.searchSimilarLectures(anyString())).thenReturn(Collections.emptyList());

			// when
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, webSocketSession);
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId, webSocketSession);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_LECTURES_FOUND);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
			assertThat(response.isAiResponse()).isTrue();
		}

		@Test
		@DisplayName("RAG 검색 중 예외 발생 시 에러 응답")
		void shouldReturnErrorWhenRagSearchFails() {
			// given
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
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
				any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
				.thenReturn(readyResponse);
			when(cacheService.cacheSimilarLectures(anyString())).thenReturn(null);
			when(ragService.searchSimilarLectures(anyString()))
				.thenThrow(new RuntimeException("RAG 검색 실패"));

			// when
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, webSocketSession);
			// 두 번째 대화에서 추천 단계로 진행하지만 RAG 검색 실패
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId, webSocketSession);

			// then
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
			// given
			Long chatRoom1 = 1L;
			Long chatRoom2 = 2L;
			String query1 = "자바 배우고 싶어요";
			String query2 = "파이썬 배우고 싶어요";
			User.MembershipLevel memberType = User.MembershipLevel.PRO;

			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query1), eq(memberType),
				any(WebSocketSession.class), eq(chatRoom1)))
				.thenReturn(ChatMessageResponse.builder().message("자바 응답").messageType(MessageType.CHAT).build());
			when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(query2), eq(memberType),
				any(WebSocketSession.class), eq(chatRoom2)))
				.thenReturn(ChatMessageResponse.builder().message("파이썬 응답").messageType(MessageType.CHAT).build());

			// when
			lectureRecommendationService.recommendationResponse(query1, memberType, chatRoom1, webSocketSession);
			lectureRecommendationService.recommendationResponse(query2, memberType, chatRoom2, webSocketSession);

			// then
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query1 + "\n"), anyMap(), eq(query1), eq(memberType),
				eq(webSocketSession), eq(chatRoom1));
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query2 + "\n"), anyMap(), eq(query2), eq(memberType),
				eq(webSocketSession), eq(chatRoom2));
		}
	}

	@Test
	@DisplayName("유사 질문이 캐시에 존재할 경우 RAG 호출 없이 응답 반환")
	void shouldReturnCachedLecturesWhenCacheHit() {
		// given
		String firstQuery = "자바 배우고 싶어요";
		String secondQuery = "취업 준비입니다.";

		// Mock ChatRoom 설정
		User mockUser = User.builder()
			.email("test@example.com")
			.password("password")
			.name("테스트유저")
			.loginType(User.LoginType.EMAIL)
			.role(User.Role.USER)
			.membershipLevel(User.MembershipLevel.PRO)
			.build();
		ChatRoom mockChatRoom = ChatRoom.builder()
			.chatroomId(chatRoomId)
			.chatroomName("새 채팅방")
			.user(mockUser)
			.type(RoomType.PRO) // RoomType enum 값 설정 필요
			.build();
		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(mockChatRoom));

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
			any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
			.thenReturn(firstResponse);
		when(aiService.analyzeConversationAndDecideNext(anyString(), anyMap(), eq(secondQuery),
			any(User.MembershipLevel.class), any(WebSocketSession.class), anyLong()))
			.thenReturn(readyResponse);
		when(cacheService.cacheSimilarLectures(anyString())).thenReturn(cachedLectures);
		when(aiService.getRecommendations(anyString(), eq(true))).thenReturn(finalResponse);
		when(braveSearchService.search(anyString())).thenReturn(mockBraveSearchResponse());

		// when
		lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId, webSocketSession);
		ChatMessageResponse response = lectureRecommendationService.recommendationResponse(secondQuery, memberType, chatRoomId, webSocketSession);

		// then
		verify(cacheService, times(1)).cacheSimilarLectures(anyString());
		verify(ragService, never()).searchSimilarLectures(anyString()); // 캐시가 있으므로 RAG 미호출
		assertThat(response.getRecommendations()).isNotNull();
	}

	// ================== Helper Methods ==================

	private Lecture createMockLecture() {
		return Lecture.builder()
			.title("스프링 입문 강의")
			.description("스프링 프레임워크 기초 학습")
			.instructor("김강사")
			.levelCode("초급")
			.thumbnailUrl("thumbnail.jpg")
			.build();
	}

	private RecommendationDto createMockRecommendation() {
		return new RecommendationDto(
			1L,
			"thumbnail.jpg",
			"스프링 입문 강의",
			"스프링 프레임워크 기초 학습",
			"김강사",
			"초급",
			"https://www.example.com/course/",
			"15000",
			"false"
		);
	}

	private BraveSearchResponseDto mockBraveSearchResponse() {
		BraveSearchResponseDto.Result result = new BraveSearchResponseDto.Result(
			"AI 기초 강의",
			"AI 입문자를 위한 강의입니다.",
			"http://example.com/course",
			new BraveSearchResponseDto.Result.ThumbnailWrapper("https://imgs.search.brave.com/thumb.jpg",
				"https://example.com/original.jpg")
		);
		BraveSearchResponseDto.Web web = new BraveSearchResponseDto.Web(List.of(result));
		return new BraveSearchResponseDto(web);
	}
}