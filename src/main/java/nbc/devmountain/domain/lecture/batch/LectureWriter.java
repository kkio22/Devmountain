package nbc.devmountain.domain.lecture.batch;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LectureWriter implements ItemWriter<Document> {

	private final VectorStore vectorStore;

	@Override
	public void write(Chunk<? extends Document> chunk) throws Exception {

	}
}
