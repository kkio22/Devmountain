package nbc.devmountain.domain.lecture.service.batch.embedding;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.model.SkillTag;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;

@RequiredArgsConstructor
@StepScope
@Slf4j
public class EmbeddingProcessor implements ItemProcessor<Lecture, Document> {

	private final LectureSkillTagRepository lectureSkillTagRepository;

	@Override
	public Document process(Lecture lecture) throws Exception {
		/*
		강의 한개를 document로 해서 그걸 모아서 writer에 500개의 document를 보냄
		 */
		try {
			String tag = lectureSkillTagRepository.findByLectureWithSkillTag(lecture).stream()
				.map(LectureSkillTag::getSkillTag)
				.map(SkillTag::getTitle)
				.collect(Collectors.joining(","));

			String content = """
				제목: %s
				강사: %s
				설명: %s
				기술 태그: %s
				""".formatted(lecture.getTitle(), lecture.getInstructor(), lecture.getDescription(), tag);

			Map<String, Object> metadata = Map.of(
				"lectureId", lecture.getLectureId(),
				"payprice", lecture.isFree() ? "0" :
					(lecture.getPayPrice() != null ? lecture.getPayPrice().toPlainString() : "0"),
				"isFree", lecture.isFree());

			Document document = new Document(UUID.randomUUID().toString(), content, metadata);

			return document;
		} catch (Exception e) {
			log.error("임베딩 실패 (lectureId: {}): {}", lecture.getLectureId(), e.getMessage());
			return null;
		}
	}
}
