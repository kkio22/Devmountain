package nbc.devmountain.domain.ai.service;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;
import nbc.devmountain.domain.ai.dto.RecommendationDto;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.user.model.User;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureRecommendationService {

	private final EmbeddingService embeddingService;
	private final RagService ragService;
	private final AiService aiService;
	private final LectureRepository lectureRepository;

	/**
	 * 사용자 입력을 기반으로 강의를 추천하는 메서드
	 * @param interest 관심사
	 * @param level 현재 수준
	 * @param goal 학습 목표
	 * @return 추천 강의 응답 DTO
	 */

	public AiRecommendationResponse recommendationResponse(String query, User.MembershipLevel memberType) {
		if (!isRecommendationQuery(query)) {
			return createDefaultResponse("안녕하세요! DevMountain 챗봇입니다. 어떤 강의를 추천해 드릴까요? 예를 들어 '자바 스프링 강의 추천해줘' 와 같이 질문해주세요.");
		}

		List<Lecture> similarLectures = ragService.searchSimilarLectures(query);
		String lectureInfo = similarLectures.stream()
			.map(l -> "제목: %s, 설명: %s".formatted(l.getTitle(), l.getDescription()))
			.collect(Collectors.joining("\n"));

		String promptText = "[사용자 질문]\n%s\n\n[유사한 강의 정보]\n%s".formatted(query, lectureInfo);
		return aiService.getRecommendations(promptText);
	}

	private boolean isRecommendationQuery(String query) {
		if (query == null || query.trim().length() < 5) return false;
		String[] keywords = {"추천", "알려줘", "강의", "강좌", "배우고", "공부", "어때"};
		for (String keyword : keywords) {
			if (query.contains(keyword)) return true;
		}
		return false;
	}

	private AiRecommendationResponse createDefaultResponse(String message) {
		// [수정] RecommendationDto 생성자를 사용합니다.
		RecommendationDto recommendation = new RecommendationDto(message, "", "", "");
		return new AiRecommendationResponse(null, Collections.singletonList(recommendation));
	}
}