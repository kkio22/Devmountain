package nbc.devmountain.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import jakarta.transaction.Transactional;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.chat.repository.ChatMessageRepository;
import nbc.devmountain.domain.chat.repository.ChatRoomRepository;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.WebSearch;
import nbc.devmountain.domain.lecture.model.Youtube;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.repository.WebSearchRepository;
import nbc.devmountain.domain.lecture.repository.YoutubeRepository;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;

@SpringBootTest
@TestPropertySource
@Transactional
@Rollback(false)
@Disabled("추천기록 더미 데이터")
class RecommendationTest {

	@Autowired
	private RecommendationRepository recommendationRepository;
	@Autowired
	private LectureRepository lectureRepository;
	@Autowired
	private YoutubeRepository youtubeRepository;
	@Autowired
	private WebSearchRepository webSearchRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Test
	void insertDummyRecommendations() {
		Random random = new Random();

		/*
		 * 테스트 시 필요한 설정
		 * 유저데이터,채팅방,강의
		 */
		User user = userRepository.findById(1L).orElseThrow();
		ChatRoom chatRoom = chatRoomRepository.findById(1L).orElseThrow();
		Lecture lecture = lectureRepository.findById(1L).orElseThrow();

		Youtube youtube = youtubeRepository.save(Youtube.builder()
			.title("유튜브 강의")
			.url("https://youtube.com/watch?v=abc123")
			.description("test description")
			.thumbnailUrl("https://example.com/jpg")
			.build());

		WebSearch webSearch = webSearchRepository.save(WebSearch.builder()
			.title("BRAVE 웹검색")
			.url("https://web.example.com/search")
			.description("test description")
			.thumbnailUrl("https://example.com/web.jpg")
			.build());

		ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.builder()
			.user(user)
			.message("강의 추천좀 해줘")
			.chatRoom(chatRoom)
			.isAiResponse(false)
			.createdAt(LocalDateTime.now())
			.build());

		// 더미 Recommendation 데이터 생성
		for (int i = 0; i < 100000; i++) {
			Recommendation.LectureType type = switch (i % 3) {
				case 0 -> Recommendation.LectureType.VECTOR;
				case 1 -> Recommendation.LectureType.YOUTUBE;
				default -> Recommendation.LectureType.BRAVE;
			};

			recommendationRepository.save(Recommendation.builder()
				.chatMessage(chatMessage)
				.user(user)
				.lecture(type == Recommendation.LectureType.VECTOR ? lecture : null)
				.youtube(type == Recommendation.LectureType.YOUTUBE ? youtube : null)
				.webSearch(type == Recommendation.LectureType.BRAVE ? webSearch : null)
				.score(random.nextFloat())
				.type(type)
				.createdAt(LocalDateTime.now().minusDays(i % 30))
				.build());
		}
	}
}
