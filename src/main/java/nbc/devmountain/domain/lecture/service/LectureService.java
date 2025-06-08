package nbc.devmountain.domain.lecture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.CategoryDto;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;
import nbc.devmountain.domain.lecture.dto.Item;
import nbc.devmountain.domain.lecture.model.LectureCategory;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.LectureCategoryMapping;
import nbc.devmountain.domain.lecture.repository.LectureCategoryRepository;
import nbc.devmountain.domain.lecture.repository.LectureCategoryMappingRepository;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Service
@RequiredArgsConstructor
public class LectureService {

	private final LectureClient lectureClient;
	private final LectureRepository lectureRepository;
	private final LectureCategoryRepository lectureCategoryRepository;
	private final LectureCategoryMappingRepository lectureCategoryMappingRepository;

	public void getLecture() {

		InflearnResponse firstPage = lectureClient.getLecture(1);

		savePage(firstPage);

		for (int i = 2; i <= firstPage.data().totalPage(); i++) {
			InflearnResponse page = lectureClient.getLecture(i);

			savePage(page);
		}

	}

	/*
	Dto -> Entity -> db 저장
	 */
	private void savePage(InflearnResponse page) {

		for (Item item : page.data().items()) {
			List<LectureCategory> lectureCategoryList = lectureCategoryRepository.saveAll(
				toCategoryEntity(item.course().metadata().categories()));

			Lecture lecture = lectureRepository.save(Lecture.builder()
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
				.payPrice(item.listPrice().payPrice())
				.regularPrice(item.listPrice().regularPrice())
				.isFree(item.listPrice().isFree())
				.discountRate(item.listPrice().discountRate())
				.build());

			lectureCategoryMappingRepository.saveAll(lectureCategoryList.stream()
				.map(c -> LectureCategoryMapping.builder()
					.lecture(lecture)
					.lectureCategory(c)
					.build())
				.toList());

		}
	}

	private List<LectureCategory> toCategoryEntity(List<CategoryDto> categoryDto) {
		return categoryDto.stream()
			.map(c -> LectureCategory.builder()
				.id(c.id())
				.title(c.title())
				.build())
			.toList();
	}

}





