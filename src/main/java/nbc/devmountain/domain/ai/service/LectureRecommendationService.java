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

	private final RagService ragService;
	private final AiService aiService;

	/**
	 * 사용자 입력을 기반으로 강의를 추천하는 메서드
	 * @param interest 관심사
	 * @param level 현재 수준
	 * @param goal 학습 목표
	 * @return 추천 강의 응답 DTO
	 */

	public AiRecommendationResponse recommendationResponse(String query, User.MembershipLevel memberType) {
		if (!isRecommendationQuery(query)) {
			return createSimpleMessage("안녕하세요! DevMountain 챗봇입니다. '자바 스프링 강의 추천해줘'와 같이 질문해 주세요.");
		}

		List<Lecture> similarLectures = ragService.searchSimilarLectures(query);
		String lectureInfo = similarLectures.stream()
			.map(l -> "제목: %s, 설명: %s".formatted(l.getTitle(), l.getDescription()))
			.collect(Collectors.joining("\n"));

		String promptText = "[사용자 질문]\n%s\n\n[유사한 강의 정보]\n%s".formatted(query, lectureInfo);
		return aiService.getRecommendations(promptText);
	}

	private boolean isRecommendationQuery(String query) {
		if (query == null || query.trim().length() < 5)
			return false;
		String[] keywords = {"추천", "알려줘", "강의", "강좌", "배우고", "공부", "어때"};
		for (String keyword : keywords) {
			if (query.contains(keyword))
				return true;
		}
		return false;
	}

	private AiRecommendationResponse createSimpleMessage(String message) {
		return new AiRecommendationResponse(message, List.of());
	}

	private boolean isAllBlank(String... arr) {
		for (String str : arr) {
			if (str != null && !str.isBlank()) return false;
		}
		return true;
	}
}