package nbc.devmountain.domain.lecture.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.user.model.User;

@Service
@RequiredArgsConstructor
public class LectureRecommendationService {

	private final LectureRepository lectureRepository;


	public String generateRecommendation(User user,String userInput) {
		// 강의 데이터 조회
		List<Lecture> lectures = lectureRepository.findRelevantLectures(userInput);

		// 강의 추천 텍스트 생성
		return "**AI 추천 강의 목록:**\n"
			+ lectures.stream()
			.limit(3)
			.map(l -> "- " + l.getName())
			.collect(Collectors.joining("\n"));
	}

	public String generateGuestRecommendation(String requirements) {
	}
}
