package nbc.devmountain.domain.ai.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	@Qualifier("redisObjectMapper")
	private final ObjectMapper objectMapper;
	private final EmbeddingModel embeddingModel;
	private static final String QUERY_EMBEDDING_KEY = "queryEmbedding";
	private static final String LECTURE_CACHE_PREFIX = "lecture";

	//새로운 질문과 비슷한 강의가 레디스에 없어서 저장 로직
	public void saveLecture(String searchQuery, List<Lecture> similarLecture) {
		float[] embedding = embeddingModel.embed(searchQuery);
		redisTemplate.opsForHash().put(QUERY_EMBEDDING_KEY, searchQuery, embedding);
		redisTemplate.opsForValue().set(LECTURE_CACHE_PREFIX + searchQuery, similarLecture, Duration.ofDays(1));
		log.info("강의가 Redis에 저장되었습니다");
	}

	//새로 들어온 질문 임베딩해서 레디스에 있는지 비교하는 로직
	public List<Lecture> cacheSimilarLectures(String searchQuery) {
		float[] currentEmbedding = embeddingModel.embed(searchQuery);

		Map<Object, Object> allEmbedding = redisTemplate.opsForHash().entries(QUERY_EMBEDDING_KEY);

		for (Map.Entry<Object, Object> embedding : allEmbedding.entrySet()) {
			String pastQuery = embedding.getKey().toString();

			Object cacheLecture = redisTemplate.opsForValue().get(LECTURE_CACHE_PREFIX + pastQuery);

			if (cacheLecture instanceof List<?> cachedList && !cachedList.isEmpty()) {

				float[] pastEmbedding = (float[])embedding.getValue();

				double similarity = cosineSimilarity(currentEmbedding, pastEmbedding);
				if (similarity > 0.9) {
					log.info("강의 유사도: {}, 강의 유사도 질문 {}", similarity, pastQuery);

					return cachedList.stream()
						.map(linkedHashMap -> mapToLecture((LinkedHashMap<String, ?>)linkedHashMap))
						//.map(linkedHashMap ->  redisObjectMapper.convertValue(linkedHashMap, Lecture.class))
						.toList();

				}
			}

		}

		return List.of();

	}

	private Lecture mapToLecture(LinkedHashMap<String, ?> linkedHashMap) {
		// ObjectMapper objectMapper = new ObjectMapper();
		// objectMapper.registerModule(new JavaTimeModule());
		// objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper.convertValue(linkedHashMap, Lecture.class);

	}

	private double cosineSimilarity(float[] currentEmbedding, float[] pastEmbedding) {
		double dot = 0.0, normA = 0.0, normB = 0.0;

		for (int i = 0; i < currentEmbedding.length; i++) {
			dot += currentEmbedding[i] * pastEmbedding[i];
			normA += Math.pow(currentEmbedding[i], 2);
			normB += Math.pow(pastEmbedding[i], 2);

		}

		return dot / (Math.sqrt(normA) * (Math.sqrt(normB)));

	}

}
