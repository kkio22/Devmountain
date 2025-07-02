package nbc.devmountain.domain.ai.service;

import java.util.List;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import nbc.devmountain.domain.lecture.model.Lecture;

import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

	private final VectorStore vectorStore;
	private final LectureRepository lectureRepository;
	private final JdbcTemplate jdbcTemplate;
	private final EmbeddingService embeddingService;

	/**
	 * 유사한 강의를 검색하는 메서드
	 * @param query 검색 쿼리
	 * @return 유사한 강의 리스트
	 */
	public List<Lecture> searchSimilarLectures(String query) {
		try {
			SearchRequest searchRequest = SearchRequest.builder()
				.query(query)
				.topK(3)
				.similarityThreshold(0.7)
				.build();

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

	/**
	 * 강의 Id로 VectorStore 임베딩값 가져오기
	 */
	public float[] getEmbeddingByLectureId(Long lectureId) {
		String sql = "SELECT embedding FROM vector_store WHERE metadata ->> 'lectureId' = ?";

		return jdbcTemplate.queryForObject(sql, new Object[] {lectureId.toString()}, (rs, rowNum) -> {
			//문자열로 변환
			String embeddingStr = rs.getString("embedding");
			//[ ] 괄호 제거
			embeddingStr = embeddingStr.replaceAll("[\\[\\]]", "");
			String[] tokens = embeddingStr.split(",");
			float[] embedding = new float[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				embedding[i] = Float.parseFloat(tokens[i].trim());
			}
			return embedding;
		});
	}

	// 코사인 유사도 계산기
	private double cosineSimilarity(float[] a, float[] b) {
		double dot = 0.0, normA = 0.0, normB = 0.0;
		for (int i = 0; i < a.length; i++) {
			dot += a[i] * b[i];
			normA += Math.pow(a[i], 2);
			normB += Math.pow(b[i], 2);
		}
		return dot / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public double calculateSimilarityWithLectureId(String query, Long lectureId) {
		try { //사용자 질문 키워드 임베딩
			float[] embedding = embeddingService.getEmbedding(query);
			//vector_store 강의 임베딩 가져오기
			float[] lectureEmbedding = getEmbeddingByLectureId(lectureId);

			double score = cosineSimilarity(embedding, lectureEmbedding);
			log.info("사용자 키워드 : '{}' 와 lectureId : {} 간 유사도 점수 = {}", query, lectureId, score);
			return score;
		} catch (Exception e) {
			log.error("강의 유사도 계산 실패 - lectureId: {}, query: {}", lectureId, query);
			return 0.0;
		}
	}
}
