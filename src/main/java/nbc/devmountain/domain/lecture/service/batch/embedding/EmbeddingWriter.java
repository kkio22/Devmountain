package nbc.devmountain.domain.lecture.service.batch.embedding;

import java.util.ArrayList;
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



		List<? extends Document> items = documents.getItems();

		log.info("들어온 강의 개수: {}", items.size());

		vectorStore.add(new ArrayList<>(items));

		log.info("임베딩 완료");
	}
}
