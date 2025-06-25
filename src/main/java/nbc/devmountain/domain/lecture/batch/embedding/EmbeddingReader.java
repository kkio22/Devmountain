package nbc.devmountain.domain.lecture.batch.embedding;

import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@RequiredArgsConstructor
@Slf4j
@StepScope
public class EmbeddingReader implements ItemReader<List<Lecture>> {

	private final LectureRepository lectureRepository;
	@Override
	public List<Lecture> read()  {

		List<Lecture> lectureList = lectureRepository.findAll();

		log.info("임베딩 강의 수: {} ", lectureList.size());


		return lectureList;
	}
}
