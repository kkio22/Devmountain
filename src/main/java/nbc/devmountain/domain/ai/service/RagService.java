package nbc.devmountain.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

	private final VectorStore vectorStore;
	private final LectureRepository lectureRepository;

	/**
	 * Lecture DB의 강의들을 벡터 DB에 저장하는 메서드
	 * DB에서 모든 강의를 꺼냄
	 * 각 강의를 벡터로 바꾼 후 Doucument 처럼 만듬
	 * vectorStore에 저장
	 */
	public void saveAllLecturesToVectorStore() {
		List<Lecture> lectures = lectureRepository.findAll();

		List<Document> documents = lectures.stream()
			.map(this::convertLectureToDocument)
			.collect(Collectors.toList());

		vectorStore.add(documents);
		log.info("저장된 강의 수: {}", documents.size());
	}

	/**
	 * 유사한 강의를 검색하는 메서드
	 * @param query 검색 쿼리
	 * @param userVector 사용자 벡터 (실제로는 query만 사용)
	 * @return 유사한 강의 리스트
	 */
	public List<Lecture> searchSimilarLectures(String query, List<Double> userVector) {
		// VectorStore에서 유사한 문서 검색
		SearchRequest searchRequest = SearchRequest.builder()
			.query(query)
			.topK(3) // 상위부터 3개 찾아온다
			.similarityThreshold(0.7)
			.build();// 유사도 임계값

		List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

		// Document에서 Lecture ID 추출하여 실제 Lecture 객체 반환
		return similarDocuments.stream()
			.map(doc -> {
				Long lectureId = Long.valueOf(doc.getMetadata().get("lectureId").toString());
				return lectureRepository.findById(lectureId).orElse(null);
			})
			.filter(lecture -> lecture != null)
			.collect(Collectors.toList());
	}

	/**
	 * Lecture를 Document로 변환
	 * 강의 -> 벡터 문서로 바꾸는 메서드
	 */
	private Document convertLectureToDocument(Lecture lecture) {
		// 강의 정보를 텍스트로 변환
		String content = String.format(
			"제목: %s, 설명: %s, 강사: %s, 레벨: %s",
			lecture.getTitle(),
			lecture.getDescription(),
			lecture.getInstructor(),
			lecture.getLevelCode()
		);
		Map<String, Object> metadata = Map.of(
			"lectureId", lecture.getLectureId(),
			"title", lecture.getTitle(),
			"levelCode", lecture.getLevelCode()
		);

		return new Document(content, metadata);
	}
}
