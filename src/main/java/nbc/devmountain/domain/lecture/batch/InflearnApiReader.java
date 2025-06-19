package nbc.devmountain.domain.lecture.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;

@Component
@RequiredArgsConstructor
public class InflearnApiReader implements ItemReader<InflearnResponse> {

	private final LectureClient lectureClient;

	@Override
	public InflearnResponse read() {


	}
}
