package nbc.devmountain.domain.ai.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.DocumentEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	@Qualifier("redisObjectMapper")
	private final ObjectMapper redisObjectMapper;
	private final RedisVectorStore redisVectorStore;
	private static final String LECTURE_CACHE_PREFIX = "lecture: ";

	//새로운 질문과 비슷한 강의가 레디스에 없어서 저장 로직
	public void storeVector(String searchQuery, List<Lecture> similarLecture) {
		List<Document> document = List.of(new Document(searchQuery));
		redisVectorStore.add(document); // RedisVectorStore에 들어온 질문 임베딩해서 redis stack에 저장 => 유사도 비교를 위해 작성
		redisTemplate.opsForValue()
			.set(LECTURE_CACHE_PREFIX + searchQuery, similarLecture,
				Duration.ofDays(1)); // 일반 redis에 들어온 query를 기준으로 강의 리스트 저장 => 질문으로 서로가 엮여있기는 하는데 저장되는 곳이 다름
		log.info("강의가 Redis에 저장되었습니다");

	}

	//새로 들어온 질문 임베딩해서 레디스에 있는지 비교하는 로직
	public List<Lecture> search(String searchQuery) {

		/*
		들어온 질문을 가지고 임베딩해서 redis stack에 저장된 임베딩된 데이터를 가지고, 유사도 0.02인 친구 중 top 1개를 가지고 나옴
		 */

		List<Document> results = redisVectorStore.similaritySearch(
			SearchRequest.query(searchQuery)
				.withSimilarityThreshold(0.95) // 작을 수록 좋은거라고 함 근데 지금 0.05도 같이 나오는 중임 필터링이 안 되는 중
				.withTopK(1)); // 그리고 레디스 스택 안의 벡터 인덱스에서 검색 -> 1개 나옴

		log.info("유사한 질문 갯수: {}", results.size());

		for(Document document : results) {
			String pastQuery = document.getContent(); // 과거 질문 (문자열)

			Object cached = redisTemplate.opsForValue().get(LECTURE_CACHE_PREFIX + pastQuery);

			log.info("강의 유사도 질문 {}", pastQuery);

			if (cached instanceof List<?> cachedList && !cachedList.isEmpty()) {
				return cachedList.stream()
					.map(linkedHashMap -> mapToLecture((LinkedHashMap<String, ?>)linkedHashMap)) // 코드
					.toList();
			}
		}

		return List.of();
	}

	private Lecture mapToLecture(LinkedHashMap<String, ?> linkedHashMap) {

		return redisObjectMapper.convertValue(linkedHashMap, Lecture.class);

	}
}

