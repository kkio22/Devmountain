package nbc.devmountain.domain.ai.service;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

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

	public AiRecommendationResponse recommendationResponse(String interest,String level,String goal){
		// 사용자 입력을 하나의 문자열로 묶기 (벡터화에 사용한다.)
		String query = interest + " " + level+ " " + goal;

		// 사용자 입력 벡터화
		List<Double> userVector = embeddingService.getEmbedding(query);

		// 유사한 강의를 벡터 DB에서 검색하기
		List<Lecture> similarLectures = ragService.searchSimilarLectures(query, userVector);

		// 사용자 문맥 문자열 생성 (LLM Prompt)
		String userContext = "관심사: %s / 수준: %s / 목표: %s".formatted(interest, level, goal);

		String lectureInfo = similarLectures.stream()
			.map(l -> "제목: %s, 설명: %s".formatted(l.getTitle(), l.getDescription()))
			.collect(Collectors.joining("\n"));

		// 유사한 강의와 사용자 정보를 바탕으로 추천하기
		return aiService.getRecommendations(userContext,lectureInfo,goal);
	}
}
