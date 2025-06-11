package nbc.devmountain.domain.ai.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.embedding.EmbeddingModel;
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
	// 	//첫 번째 결과에서 벡터 값을 꺼내서 반환
	// 	return response.getResults().get(0).getOutput();
	// }
	private final EmbeddingModel embeddingModel;

	/**
	 * 단일 텍스트를 입력 받아 임베딩 벡터를 반환
	 * getOutput() : 첫 번째 임베딩 결과에서 float 배열 형태의 벡터 추출 / 실제 텍스트 수치적 표현
	 * @return 임베딩 벡터 (List<Double>)
	 */
	public List<Double> getEmbedding(String content) {
		// 1. 텍스트를 리스트로 감싸서 요청
		EmbeddingResponse response = embeddingModel.embedForResponse(List.of(content));

		// 2. 응답 결과 검증
		if (response.getResults().isEmpty()) {
			throw new IllegalStateException("임베딩 결과가 존재하지 않습니다.");
		}

		float[] embedding = response.getResults().get(0).getOutput();
		if (embedding == null) {
			throw new IllegalStateException("임베딩 결과 벡터가 null입니다.");
		}

		// float 배열을 Double 리스트로 변환
		List<Double> listEmbedding = new ArrayList<>();
		for (float value : embedding) {
			listEmbedding.add((double)value);
		}

		return listEmbedding;
	}
}

