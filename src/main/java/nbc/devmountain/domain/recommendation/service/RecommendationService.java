package nbc.devmountain.domain.recommendation.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.user.model.User;

@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final LectureRepository lectureRepository;

	public String generateRecommendation(String userInput) {
		// 강의 데이터 조회
		List<Lecture> lectures = lectureRepository.findRelevantLectures(userInput);

		if (lectures.isEmpty()) {
			return "해당 조건에 맞는 추천 강의가 없습니다.";
		}

		return "**AI 추천 강의 목록:**\n"
			+ lectures.stream()
			.limit(3)
			.map(l -> "- " + l.getTitle())
			.collect(Collectors.joining("\n"));
	}
}
