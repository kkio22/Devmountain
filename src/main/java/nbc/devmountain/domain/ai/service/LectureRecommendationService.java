package nbc.devmountain.domain.ai.service;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

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

	public AiRecommendationResponse recommendationResponse(String query, User.MembershipLevel memberType){
		// 사용자 입력 벡터화
		List<Double> queryVector = embeddingService.getEmbedding(query);

		// 유사한 강의를 벡터 DB에서 검색하기
		List<Lecture> similarLectures = ragService.searchSimilarLectures(query);

		String lectureInfo = similarLectures.stream()
			.map(l -> "제목: %s, 설명: %s".formatted(l.getTitle(), l.getDescription()))
			.collect(Collectors.joining("\n"));
		//todo:멤버십 별 분기 추가해야함

		// 사용자 문맥 문자열 생성 (LLM Prompt)
		String promptText = """
            [사용자 질문]
            %s

            [유사한 강의 정보]
            %s

            응답 형식 예시:
            {
              "recommendations": [
                {
                  "title": "실전 자바 백엔드 개발",
                  "url": "https://example.com/java-backend",
                  "level": "중급",
                  "thumbnailUrl": null
                }
              ]
            }
            """.formatted(query, lectureInfo);


		// 유사한 강의와 사용자 정보를 바탕으로 추천하기
		return aiService.getRecommendations(promptText);
	}
}
