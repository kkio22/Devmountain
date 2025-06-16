package nbc.devmountain.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.service.EmbeddingService;
import nbc.devmountain.domain.lecture.service.LectureService;

@Component
@RequiredArgsConstructor
public class LectureRunner implements CommandLineRunner {

	private final LectureService lectureService;
	private final EmbeddingService embeddingService;

	@Override
	public void run(String... args) throws Exception {
		lectureService.getLecture();
		embeddingService.embedLecture();
	}
}
