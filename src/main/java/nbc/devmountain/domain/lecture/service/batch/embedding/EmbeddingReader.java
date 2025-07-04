package nbc.devmountain.domain.lecture.service.batch.embedding;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@RequiredArgsConstructor
@Slf4j
@StepScope
public class EmbeddingReader implements ItemReader<Lecture> {

	private final LectureRepository lectureRepository;
	private static final int pageSize = 500;
	private int currentPage = 0; // 500개씩 끊은 강의 페이지 몇 번째인지
	private int currentIndex = 0; // 그 500개에서 몇번째 강의를 하고 있는지 -> 하나씩 넘기니까
	private List<Lecture> lectureList = new ArrayList<>();

	@Override
	public Lecture read() { // 이걸 호출해서 계속 데이터 받아감

		if (lectureList.isEmpty() || currentIndex >= lectureList.size()) { //일단 처음 시작할 때 강의가 없어야 하고, 500개 이상이 되면 끝내고

			PageRequest pageRequest = PageRequest.of(currentPage++, pageSize); // 1페이지의 500개, 2페이지의 500개 이런식으로 묶어서

			Page<Lecture> lecturePage = lectureRepository.findAll(pageRequest); //500개 가지고 나옴

			lectureList = lecturePage.getContent(); //그걸 list에 당음
			currentIndex = 0;

			if (lectureList.isEmpty()) { // 더이상 없으면 step 종료
				return null;
			}

			log.info("읽은 페이지: {}, 강의 수: {}", currentPage, lectureList.size());

		}
		return lectureList.get(currentIndex++); //그리고 거기서 1~500개 차례대로 보냄

	}
}
