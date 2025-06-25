package nbc.devmountain.domain.lecture.batch.crawling;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;
import nbc.devmountain.domain.lecture.dto.LectureWithSkillTag;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.SkillTag;
import nbc.devmountain.domain.lecture.repository.SkillTagRepository;


@Slf4j
@RequiredArgsConstructor
@StepScope
public class InflearnApiProcessor implements ItemProcessor<InflearnResponse, List<LectureWithSkillTag>> {

	private final SkillTagRepository skillTagRepository;

	@Override
	public List<LectureWithSkillTag> process(InflearnResponse inflearnResponse) {

		return inflearnResponse.data().items().stream().map(
				item -> {

					Lecture lecture = Lecture.builder()
						.itemId(item.id())
						.thumbnailUrl(item.course().thumbnailUrl())
						.title(item.course().title())
						.instructor(item.instructor().name())
						.description(item.course().description())
						.reviewCount(item.course().reviewCount())
						.studentCount(item.course().studentCount())
						.likeCount(item.course().likeCount())
						.star(item.course().star())
						.levelCode(item.course().metadata().levelCode())
						.isDiscount(item.listPrice().isDiscount())
						.payPrice(BigDecimal.valueOf(item.listPrice().payPrice()))
						.regularPrice(BigDecimal.valueOf(item.listPrice().regularPrice()))
						.isFree(item.listPrice().isFree())
						.discountRate(BigDecimal.valueOf(item.listPrice().discountRate()))
						.crawledAt(LocalDateTime.now())
						.build();

					List<SkillTag> tags = item.course().metadata().skillTags().stream()
						.map(tag -> findOrCreateSkillTag(tag.title()))
						.toList();

					return new LectureWithSkillTag(lecture, tags);

				}
			)
			.toList();

	}

	private SkillTag findOrCreateSkillTag(String title) {
		return skillTagRepository.findByTitle(title)
			.orElseGet(() -> skillTagRepository.save(new SkillTag(title)));
	}

}
