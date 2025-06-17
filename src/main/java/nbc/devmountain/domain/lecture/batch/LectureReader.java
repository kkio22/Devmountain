package nbc.devmountain.domain.lecture.batch;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Component
@StepScope
@RequiredArgsConstructor
public class LectureReader implements ItemReader<Lecture> {

	private final LectureRepository lectureRepository;

	@Value("#{jobParameters['full'] ?: 'false'}")
	private String full;

	private Iterator<Lecture> lectureIterator;

	@Override
	public Lecture read() {
		if (lectureIterator == null) {
			boolean isFull = "true".equalsIgnoreCase(full);
			List<Lecture> lectureList =
				isFull ? lectureRepository.findAll() : lectureRepository.findByIsEmbeddedFalse();
			lectureIterator = lectureList.iterator();

		}
		return lectureIterator.hasNext() ? lectureIterator.next() : null;
	}
}
