package nbc.devmountain.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

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
	private final JdbcTemplate jdbcTemplate;
	private final LectureSkillTagRepository lectureSkillTagRepository;

	/**
	 * Lecture DB의 강의들을 벡터 DB에 저장하는 메서드
	 * DB에서 모든 강의를 꺼냄
	 * 각 강의를 벡터로 바꾼 후 Document 처럼 만듬
	 * vectorStore에 저장
	 */
	public void saveEmbeddedLecturesToVectorStore() {
		List<Lecture> embeddedLectures = lectureRepository.findAll();

		if (embeddedLectures.isEmpty()) {
			log.warn("저장할 강의가 없습니다.");
			return;
		}

		int addedOrUpdatedCount = 0;
		for (Lecture lecture : embeddedLectures) {
			try {
				UUID existingDocId = findVectorStoreIdByLectureId(lecture.getLectureId());
				Document docToSave;
				Document originalDoc = convertLectureToDocument(lecture);
				if (existingDocId != null) {
					docToSave = new Document(existingDocId.toString(), originalDoc.getContent(),
						originalDoc.getMetadata());
					log.info("기존 벡터 업데이트: lectureId={}, docId={}", lecture.getLectureId(), existingDocId);
				} else {
					docToSave = originalDoc;
					log.info("새 벡터 추가: lectureId={}", lecture.getLectureId());
				}

				vectorStore.add(List.of(docToSave));
				addedOrUpdatedCount++;

			} catch (Exception e) {
				log.error("벡터 저장/업데이트 중 오류: lectureId={}, error={}", lecture.getLectureId(), e.getMessage(), e);
			}
		}

		log.info("벡터 스토어에 새로 추가되거나 업데이트된 강의 수: {}", addedOrUpdatedCount);
	}


	private UUID findVectorStoreIdByLectureId(Long lectureId) {
		String sql = "SELECT id FROM vector_store WHERE (metadata->>'lectureId')::bigint = ? LIMIT 1";
		try {
			return jdbcTemplate.queryForObject(sql, UUID.class, lectureId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			log.error("lectureId={}에 해당하는 벡터 스토어 ID를 찾는 중 오류 발생: {}", lectureId, e.getMessage());
			return null;
		}
	}

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
				.filter(lecture -> lecture != null)
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("벡터 검색 실패: {}", e.getMessage(), e);
			return fallbackSearch(query);
		}
	}

	private List<Lecture> fallbackSearch(String query) {
		log.info("키워드로 대체 검색 : {}", query);
		return lectureRepository.findTop5ByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
	}

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
			"instructor", lecture.getInstructor() != null ? lecture.getInstructor() : "",
			"description", lecture.getDescription() != null ? lecture.getDescription() : "",
			"levelCode", lecture.getLevelCode() != null ? lecture.getLevelCode() : "미정",
			"thumbnailUrl", lecture.getThumbnailUrl() != null ? lecture.getThumbnailUrl() : ""
		);
		return new Document(content, metadata);
	}
}