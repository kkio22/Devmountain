package nbc.devmountain.domain.lecture.model;

import java.util.List;

import org.springframework.stereotype.Component;

import nbc.devmountain.domain.lecture.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.lecture.dto.Item;
import nbc.devmountain.domain.lecture.repository.LectureCategoryRepository;

@Component
@RequiredArgsConstructor
public class LectureMapper {
	private final LectureCategoryRepository LecturecategoryRepository;

	/*
	강의가 배열이어서 하나의 강의를 꺼내서 거기있는 course, instrutor, listprice를 dto -> entity로 매핑
	그 과정에서 list로 되어있는 metadata에 있는 category는 매서드로 처리
	 */
	public Lecture toEntity(Item item) {

		List<LectureCategory> categoryList = toCategoryEntity(item.course().metadata().categories());

		LecturecategoryRepository.saveAll(categoryList);

		return Lecture.builder()
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
			.build();
	}

	/*
	상위 카테고리가 필요한가?-> 일단 parent는 설정안 한 로직임
	 */
	private List<LectureCategory> toCategoryEntity(List<CategoryDto> categoryDto) {
		return categoryDto.stream()
			.map(c -> LectureCategory.builder()
				.id(c.id())
				.title(c.title())
				.build())
			.toList();
	}

}