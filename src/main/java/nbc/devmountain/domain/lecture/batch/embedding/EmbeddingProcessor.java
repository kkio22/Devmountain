package nbc.devmountain.domain.lecture.batch.embedding;

import java.util.List;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;

@RequiredArgsConstructor
@StepScope
@Slf4j
public class EmbeddingProcessor implements ItemProcessor<List<Lecture>, VectorStore> {
	@Override
	public VectorStore process(List<Lecture> item) throws Exception {
		/*
		가져온 강의 데이터 500개를 가공해서 document 형태로 writer에 보내야 함
		 */
		return
	}
}
