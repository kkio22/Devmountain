package nbc.devmountain.domain.lecture.batch.embedding;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@StepScope
@RequiredArgsConstructor
@Slf4j
public class EmbeddingWriter implements ItemWriter<Document> {
	private final VectorStore vectorStore;

	@Override
	public void write(Chunk<? extends Document> documents) throws Exception {

	/*
	chunk<Document> document 형태 즉 List<Documnet> document이고 document 500개가 한번에 들어온 것임
	 */

		log.info("들어온 강의 개수: {}", documents.size());

		vectorStore.add((List<Document>)documents);

		log.info("임베딩 완료");
	}
}
