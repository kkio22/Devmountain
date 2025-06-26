package nbc.devmountain.domain.lecture.batch.crawling;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;

@Slf4j
@RequiredArgsConstructor
@StepScope
public class InflearnApiReader implements ItemReader<InflearnResponse> {
	/*
	chunk 단위가 페이지 1개임
	그러면 그 페이지 40개의 강의가 들어옴
	processor에도 1페이지 40개 강의가 가공되어서
	writer에서 페이지 여러개가 한번에 올 수 있는 구조인건가?
	 */

	private final LectureClient lectureClient;
	private int currentPage = 1;
	private int totalPage = -1;

	@Override
	public InflearnResponse read() {

		if (totalPage != -1 && currentPage > totalPage) {
			return null;
		}

		try {

			InflearnResponse inflearnResponse = lectureClient.getLecture(currentPage);
			log.info("현재 페이지: {}", currentPage);

			if (totalPage == -1) {
				totalPage = inflearnResponse.data().totalPage();
			}

			currentPage++;
			return inflearnResponse;

		}catch (Exception e){
			log.error("크롤링 중 예외 발생: {}", e.getMessage());
			throw new RuntimeException("강의 크롤링에서 에러가 발생했습니다.");
		}


	}
}

