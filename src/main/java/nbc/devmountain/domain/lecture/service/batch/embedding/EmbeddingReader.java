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
	/*
	한번에 db에서 가지고 오는 강의 갯수
	왜냐하면 예전 코드는 전체를 가지고 와서 메모리에서 하는 거였는데 이러면 강의 수가 많아질수록 메모리 사용량이 높아지니
	안 좋을 것 같았음 그래서 적절한 갯수로 잘라서 db 접근을 해서 강의 하나씩 접근을 하지는 않지만 접근은 함 그렇다고 메모리에서 모든걸 실행시키지도 않게 하는게 좋겠다고 생각해서
	잘라서 db 접근해서 한개씩 넘기는 구조 사용함 => 이게 맞는지는 모르겠음...
	 */
	private static final int pageSize = 500;
	private int currentPage = 0; // 500개씩 끊은 강의 페이지 몇 번째인지
	private int currentIndex = 0; // 그 500개에서 몇번째 강의를 하고 있는지 -> 하나씩 넘기니까
	private List<Lecture> lectureList = new ArrayList<>();

	@Override
	public Lecture read() { // 이걸 호출해서 계속 데이터 받아감
		/*
		저장된 강의를 가지고 나옴
		여기서 가지고 온 강의는 이미 삭제될거는 삭제되고, 업데이트가 된 상태
		그리고 그 삭제된 것과 업데이트된 것만 가져와서 가공하는게 좋을 것 같음

		그리고 내가 chunk 단위를 500개로 하면 reader processor는 강의 1개를 기준으로 단위를 짜도 알아서 writer 호출할 때 500개 모아서 호출함
		 */
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
