package nbc.devmountain.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import nbc.devmountain.domain.search.sevice.BraveSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nbc.devmountain.domain.ai.constant.AiConstants;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.search.dto.BraveSearchResponseDto;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.MessageType;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.user.model.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureRecommendationService 단위 테스트")
class LectureRecommendationServiceTest {

	@Mock
	private RagService ragService;

	@Mock
	private AiService aiService;

	@Mock
	private BraveSearchService braveSearchService;

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
				.recommendationResponse(emptyQuery, memberType, chatRoomId);

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
				.recommendationResponse(null, memberType, chatRoomId);

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
				.recommendationResponse(query, memberType, null);

			// then
			assertThat(response.getMessage()).isEqualTo(AiConstants.ERROR_NO_CHATROOM);
			assertThat(response.getMessageType()).isEqualTo(MessageType.ERROR);
		}

		@Test
		@DisplayName("처리 중 예외 발생 시 에러 응답 반환")
		void shouldReturnErrorWhenExceptionOccurs() {
			// given
			String query = "자바 배우고 싶어요";
			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), anyString()))
				.thenThrow(new RuntimeException("AI 서비스 에러"));

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(query, memberType, chatRoomId);

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

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(query)))
				.thenReturn(mockResponse);

			// when
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(query, memberType, chatRoomId);

			// then
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query + "\n"),
				any(),
				eq(query)
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

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(firstQuery)))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(secondQuery)))
				.thenReturn(secondResponse);

			// when
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId);
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId);

			// then
			verify(aiService, times(2)).analyzeConversationAndDecideNext(anyString(), any(), anyString());
			assertThat(response.getMessage()).contains("프로그래밍 경험");
			assertThat(response.getMessageType()).isEqualTo(MessageType.CHAT);
		}

		@Test
		@DisplayName("충분한 정보 수집 후 추천 단계로 진행")
		void shouldProceedToRecommendationWhenInfoCollected() {
			// given
			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업 준비용으로요";
			List<Lecture> mockLectures = List.of(createMockLecture());
			List<RecommendationDto> mockRecommendations = List.of(createMockRecommendation());

			// 첫 번째 대화 응답 (추가 질문)
			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();

			// 두 번째 대화 응답 (추천 준비 완료)
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

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(firstQuery)))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(secondQuery)))
				.thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString())).thenReturn(mockLectures);
			when(braveSearchService.search(anyString())).thenReturn(mockBraveSearchResponse());
			when(aiService.getRecommendations(anyString(), eq(true))).thenReturn(finalResponse);

			// when - 첫 번째 대화
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId);
			// 두 번째 대화에서 추천 단계로 진행
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId);

			// then
			verify(ragService, times(1)).searchSimilarLectures(anyString());
			verify(aiService, times(1)).getRecommendations(anyString(), eq(true));
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
			// given
			String firstQuery = "자바 배우고 싶어요";
			String secondQuery = "취업 준비용으로요";
			
			// 첫 번째 대화 응답 (추가 질문)
			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			
			// 두 번째 대화 응답 (추천 준비 완료)
			ChatMessageResponse readyResponse = ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(firstQuery)))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(secondQuery)))
				.thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString())).thenReturn(Collections.emptyList());

			// when - 첫 번째 대화
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId);
			// 두 번째 대화에서 추천 단계로 진행하지만 강의가 없음
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId);

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
			
			// 첫 번째 대화 응답 (추가 질문)
			ChatMessageResponse firstResponse = ChatMessageResponse.builder()
				.message("어떤 목적으로 자바를 배우고 싶으신가요?")
				.isAiResponse(true)
				.messageType(MessageType.CHAT)
				.build();
			
			// 두 번째 대화 응답 (추천 준비 완료)
			ChatMessageResponse readyResponse = ChatMessageResponse.builder()
				.message(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION)
				.isAiResponse(true)
				.messageType(MessageType.RECOMMENDATION)
				.build();

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(firstQuery)))
				.thenReturn(firstResponse);
			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), eq(secondQuery)))
				.thenReturn(readyResponse);
			when(ragService.searchSimilarLectures(anyString()))
				.thenThrow(new RuntimeException("RAG 검색 실패"));

			// when - 첫 번째 대화
			lectureRecommendationService.recommendationResponse(firstQuery, memberType, chatRoomId);
			// 두 번째 대화에서 추천 단계로 진행하지만 RAG 검색 실패
			ChatMessageResponse response = lectureRecommendationService
				.recommendationResponse(secondQuery, memberType, chatRoomId);

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

			when(aiService.analyzeConversationAndDecideNext(anyString(), any(), anyString()))
				.thenReturn(ChatMessageResponse.builder()
					.message("응답")
					.messageType(MessageType.CHAT)
					.build());

			// when
			lectureRecommendationService.recommendationResponse(query1, memberType, chatRoom1);
			lectureRecommendationService.recommendationResponse(query2, memberType, chatRoom2);

			// then
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query1 + "\n"), any(), eq(query1));
			verify(aiService).analyzeConversationAndDecideNext(
				eq("사용자: " + query2 + "\n"), any(), eq(query2));
		}
	}

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
			"초급"
		);
	}

	private BraveSearchResponseDto mockBraveSearchResponse() {
		BraveSearchResponseDto.Result result = new BraveSearchResponseDto.Result(
				"AI 기초 강의",
				"AI 입문자를 위한 강의입니다.",
				new BraveSearchResponseDto.Result.ThumbnailWrapper("https://imgs.search.brave.com/thumb.jpg", "https://example.com/original.jpg")
		);
		BraveSearchResponseDto.Web web = new BraveSearchResponseDto.Web(List.of(result));
		return new BraveSearchResponseDto(web);
	}
} 