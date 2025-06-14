package nbc.devmountain.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import lombok.extern.slf4j.Slf4j;
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
	private final JdbcTemplate jdbcTemplate;

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
				// 1. lectureId로 기존 Document ID(UUID)를 찾습니다.
				UUID existingDocId = findVectorStoreIdByLectureId(lecture.getLectureId());

				// 2. 새로운 Document 객체를 생성하거나, 기존 Document의 ID를 포함하여 재생성합니다.
				Document docToSave;
				if (existingDocId != null) {
					// 기존 Document가 있다면, 해당 UUID를 사용하여 Document를 재생성하여 업데이트를 유도합니다.
					Document originalDoc = convertLectureToDocument(lecture); // ID 없는 Document 생성
					docToSave = new Document(existingDocId.toString(), originalDoc.getContent(), originalDoc.getMetadata());
					log.info("기존 벡터 업데이트: lectureId={}, docId={}", lecture.getLectureId(), existingDocId);
				} else {
					// 새 Document라면, vectorStore.add() 내부에서 UUID가 자동으로 생성됩니다.
					docToSave = convertLectureToDocument(lecture); // ID 없는 Document 생성
					log.info("새 벡터 추가: lectureId={}", lecture.getLectureId());
				}

				// Spring AI의 VectorStore.add()는 Document.id가 존재하는 경우 업데이트, 없는 경우 삽입을 시도합니다.
				vectorStore.add(List.of(docToSave));
				addedOrUpdatedCount++;

			} catch (Exception e) {
				log.error("벡터 저장/업데이트 중 오류: lectureId={}, error={}", lecture.getLectureId(), e.getMessage(), e);
			}
		}

		log.info("벡터 스토어에 새로 추가되거나 업데이트된 강의 수: {}", addedOrUpdatedCount);
	}

	/**
	 * vector_store 테이블에서 lectureId를 이용해 기존 Document의 UUID를 찾습니다.
	 */
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
			"description",lecture.getDescription() != null ? lecture.getDescription() : "",
			"levelCode", lecture.getLevelCode() != null ? lecture.getLevelCode() : "미정",
			"tags",tags != null ? tags : ""
		);

		// Document 생성 시 ID는 비워둡니다. (vectorStore.add가 처리)
		// 업데이트 시에는 위에 있는 로직에서 existingDocId로 새 Document를 생성합니다.
		return new Document(content, metadata);
	}
}
