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
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.model.SkillTag;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

	private final VectorStore vectorStore;
	private final LectureRepository lectureRepository;
	private final LectureSkillTagRepository lectureSkillTagRepository;

	/**
	 * Lecture DB의 강의들을 벡터 DB에 저장하는 메서드
	 * DB에서 모든 강의를 꺼냄
	 * 각 강의를 벡터로 바꾼 후 Doucument 처럼 만듬
	 * vectorStore에 저장
	 */
	public void saveEmbeddedLecturesToVectorStore() {
		// 임베딩이 완료된 강의들만 조회
		List<Lecture> embeddedLectures = lectureRepository.findByIsEmbeddedTrue();

		if (embeddedLectures.isEmpty()) {
			log.warn("임베딩된 강의가 없습니다. EmbeddingService를 먼저 실행하세요.");
			return;
		}

		List<Document> documents = embeddedLectures.stream()
			.map(this::convertLectureToDocument)
			.collect(Collectors.toList());

		vectorStore.add(documents);
		log.info("벡터 스토어에 저장된 강의 수: {}", documents.size());
	}

	/**
	 * 유사한 강의를 검색하는 메서드
	 * @param query 검색 쿼리
	 * @return 유사한 강의 리스트
	 */
	public List<Lecture> searchSimilarLectures(String query) {
		try {
			// VectorStore에서 유사한 문서 검색 - M4 버전의 새로운 API 사용
			SearchRequest searchRequest = SearchRequest.query(query)
				.withTopK(3) // 상위부터 3개 찾아온다
				.withSimilarityThreshold(0.7); // 유사도 임계값

			List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
			log.info("검색 쿼리: {}", query);
			log.info("검색된 문서 수: {}", similarDocuments.size());

			// Document에서 Lecture ID 추출하여 실제 Lecture 객체 반환
			return similarDocuments.stream()
				.map(doc -> {
					Long lectureId = Long.valueOf(doc.getMetadata().get("lectureId").toString());
					return lectureRepository.findById(lectureId).orElse(null);
				})
				.filter(lecture -> lecture != null)
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("벡터 검색 실패: {}", e.getMessage(), e);
			// 검색 실패시 : 키워드 검색
			return fallbackSearch(query);
		}
	}

	private List<Lecture> fallbackSearch(String query) {
		log.info("키워드로 대체 검색 : {}", query);
		return lectureRepository.findTop5ByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
	}

	/**
	 * Lecture를 Document로 변환
	 * 강의 -> 벡터 문서로 바꾸는 메서드
	 */
	private Document convertLectureToDocument(Lecture lecture) {

		String tags = "";
		try {
			tags=lectureSkillTagRepository.findByLecture(lecture).stream()
				.map(LectureSkillTag::getSkillTag)
				.map(SkillTag::getTitle)
				.collect(Collectors.joining(","));
		}catch (Exception e) {
			log.debug("스킬 태그 로드 실패");
		}

		// 강의 정보를 텍스트로 변환
		String content = String.format(
			"제목: %s\n강사: %s\n설명: %s\n기술 태그: %s",
			lecture.getTitle(),
			lecture.getInstructor(),
			lecture.getDescription() != null ? lecture.getDescription() : "",
			tags
		);

		Map<String, Object> metadata = Map.of(
			"lectureId", lecture.getLectureId(),
			"title", lecture.getTitle() != null ? lecture.getTitle() : "",
			"levelCode", lecture.getLevelCode() != null ? lecture.getLevelCode() : "미정"
		);

		return new Document(content, metadata);
	}
}
