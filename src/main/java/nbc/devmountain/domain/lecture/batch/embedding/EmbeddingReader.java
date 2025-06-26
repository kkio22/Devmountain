package nbc.devmountain.domain.lecture.batch.embedding;

import java.util.Iterator;
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
public class EmbeddingReader implements ItemReader<Lecture> {

	private final LectureRepository lectureRepository;
	private final int  = 500;
	@Override
	public Lecture read()  {
		/*
		저장된 강의를 가지고 나옴
		여기서 가지고 온 강의는 이미 삭제될거는 삭제되고, 업데이트가 된 상태
		그리고 그 삭제된 것과 업데이트된 것만 가져와서 가공하는게 좋을 것 같음

		그리고 내가 chunk 단위를 500개로 하면 reader processor는 강의 1개를 기준으로 단위를 짜도 알아서 writer 호출할 때 500개 모아서 호출함

		 */








	}
}
