package nbc.devmountain.domain.lecture.batch.crawling;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;

import io.lettuce.core.dynamic.batch.BatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;

@Slf4j
@RequiredArgsConstructor
@StepScope
public class InflearnApiReader implements ItemReader<InflearnResponse> {

	private final LectureClient lectureClient;
	private int currentPage = 1;
	private int totalPage = 0;

	@Override
	public InflearnResponse read() {

		if (totalPage != 0 && currentPage > totalPage) {
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
			throw new BatchException(BatchExceptionCode.LECTURE_API_FAILED);
		}

	}
}

