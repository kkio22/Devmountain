package nbc.devmountain.domain.ai.service;

import java.util.List;

import java.util.Objects;


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
	 * 유사한 강의를 검색하는 메서드
	 * @param query 검색 쿼리
	 * @return 유사한 강의 리스트
	 */
	public List<Lecture> searchSimilarLectures(String query) {
		try {
			SearchRequest searchRequest = SearchRequest.query(query)
				.withTopK(3)
				.withSimilarityThreshold(0.7);

			List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
			log.info("검색 쿼리: {}", query);
			log.info("검색된 문서 수: {}", similarDocuments.size());

			return similarDocuments.stream()
				.map(doc -> {
					Long lectureId = Long.valueOf(doc.getMetadata().get("lectureId").toString());
					return lectureRepository.findById(lectureId).orElse(null);
				})
				.filter(Objects::nonNull)
				.toList();
		} catch (Exception e) {
			log.error("벡터 검색 실패: {}", e.getMessage(), e);
			return fallbackSearch(query);
		}
	}

	/**
	 * 벡터 검색 실패시 대체실행
	 * */
	private List<Lecture> fallbackSearch(String query) {
		log.info("키워드로 대체 검색 : {}", query);
		return lectureRepository.findTop5ByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
	}
}