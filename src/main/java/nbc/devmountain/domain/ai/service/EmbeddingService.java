package nbc.devmountain.domain.ai.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

// import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmbeddingService {

	/**
	 * Spring AI 0.8.0 기준 -> OpenAiEmbeddingClient 자동 빈 등록 X
	 * embeddingClient를 Bean으로 등록해야 가능
	 * -> Config 파일 생성해서 직접 Bean 등록
	 */
	// private final OpenAiEmbeddingClient embeddingClient;
	//
	// public List<Double> getEmbedding(String content) {
	// 	EmbeddingRequest request = new EmbeddingRequest(List.of(content), null);
	// 	EmbeddingResponse response = embeddingClient.call(request);
	// 	return response.getResults().get(0).getOutput();
	// }
	/**
	 * embedForResponse(List.of(content)) -> 하나의 텍스트를 벡터로 변환
	 * response.getResults().get().getOutput() -> 첫 번째 벡터 결과 꺼내기
	 */
	// private final EmbeddingModel embeddingModel;
	//
	// public List<Double> getEmbedding(String content) {
	// 	// 입력 문자열을 리스트로 감싼 후 모델에 전달
	// 	EmbeddingResponse response = embeddingModel.embedForResponse(List.of(content));
	//
	// 	// 첫 번째 결과에서 벡터 값을 꺼내서 반환
	// 	return response.getResults().get(0).getOutput();
	// }
}

