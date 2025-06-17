package nbc.devmountain.domain.lecture.batch;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.model.Lecture;

@Component
@RequiredArgsConstructor
public class LectureProcessor implements ItemProcessor<Lecture, Document> {
	@Override
	public Document process(Lecture item) throws Exception {
		return null;
	}
}
