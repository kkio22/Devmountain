package nbc.devmountain.domain.lecture.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;
import nbc.devmountain.domain.lecture.dto.Item;
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.model.SkillTag;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.SkillTagRepository;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Service
@RequiredArgsConstructor
public class LectureService {

	private final LectureClient lectureClient;
	private final LectureRepository lectureRepository;
	private final SkillTagRepository skillTagRepository;
	private final LectureSkillTagRepository lectureSkillTagRepository;

	public void getLecture() {

		InflearnResponse firstPage = lectureClient.getLecture(1);

		for (int i = 1; i <= firstPage.data().totalPage(); i++) {
			InflearnResponse page = lectureClient.getLecture(i);

			savePage(page);
		}

	}


	private void savePage(InflearnResponse page) {
		List<Lecture> lectures = new ArrayList<>();
		Map<Lecture, List<SkillTag>> lectureSkillTagMap = new HashMap<>();

		for (Item item : page.data().items()) {

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
				.build();

			lectures.add(lecture);


			List<SkillTag> tags = item.course().metadata().skillTags().stream()
				.map(tag -> findOrCreateSkillTag(tag.title()))
				.toList();

			lectureSkillTagMap.put(lecture, tags);
		}


		List<Lecture> savedLectures = lectureRepository.saveAll(lectures);

		List<LectureSkillTag> lectureSkillTags = new ArrayList<>();

		for (Lecture lecture : savedLectures) {
			for (SkillTag tag : lectureSkillTagMap.get(lecture)) {
				if (!lectureSkillTagRepository.existsByLectureAndSkillTag(lecture, tag)) {
					lectureSkillTags.add(new LectureSkillTag(tag, lecture));
				}
			}
		}

		lectureSkillTagRepository.saveAll(lectureSkillTags);
	}


	private SkillTag findOrCreateSkillTag(String title) {
		return skillTagRepository.findByTitle(title)
			.orElseGet(() -> skillTagRepository.save(new SkillTag(title)));
	}

}






