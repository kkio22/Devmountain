package nbc.devmountain.domain.lecture.service.batch.crawling;

import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.dto.LectureWithSkillTag;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;


@Slf4j
@StepScope
@RequiredArgsConstructor
public class InflearnApiWriter implements ItemWriter<List<LectureWithSkillTag>> {
	private final LectureRepository lectureRepository;
	private final LectureSkillTagRepository lectureSkillTagRepository;

	@Override
	public void write(Chunk<? extends List<LectureWithSkillTag>> items) {


		for (List<LectureWithSkillTag> lectureWithSkillTag : items) {

			/*
			강의 40개만 추출
			 */
			List<Lecture> lectures = lectureWithSkillTag.stream()
				.map(LectureWithSkillTag::lecture)
				.toList();

			/*
			강의 40개의 고유 id 값 추출
			 */
			List<Integer> itemId = lectures.stream()
				.map(Lecture::getItemId)
				.toList();

			/*
			그 id값으로 강의가 이미 있는지 없는지 확인 -> 있으면 있는 강의를 가지고 나오니 그 강의의 id값을 얻는거임
			 */
			List<Lecture> existLecture = lectureRepository.findAllByItemIdIn(itemId);

			List<Integer> existItemId = existLecture.stream()
				.map(Lecture::getItemId)
				.toList();

			/*
			filter를 사용해서 해당 강의 id 값만 강의 추출
			 */
			List<Lecture> saveLecture = lectures.stream() //강의 stream으로 바꿈
				.filter(lecture -> !existItemId.contains(
					lecture.getItemId())) // filter를 하나의 강의 가지고 나와서 그 강의 itemId가 existItemId에 포함이 안 되는 것만 필터링
				.toList();

			/*
			강의 저장
			 */
			lectureRepository.saveAll(saveLecture);

			/*
			강의가 새로 저장될 때 lectureSkillTagRepository에 저장
		     */

			List<Integer> saveItemId = saveLecture.stream()
				.map(Lecture::getItemId)
				.toList();

			List<LectureSkillTag> lectureSkillTags = lectureWithSkillTag.stream()// 하나의 강의에 list로 스킬 태그가 저장되어있음 즉, 스킬 태그는 이중 list 상태임
				.filter(lecture-> saveItemId.contains(lecture.lecture().getItemId())) // 하나의 강의 itemid가 saveItemId에 포함되면 태그도 저장
				.flatMap(skillTag -> skillTag.skillTags()
					.stream() //flatmap을 사용해서 list<skilltag>로 풀고 거기서
					.map(tag -> new LectureSkillTag(tag, skillTag.lecture()))) //한번 더 map 사용해서 하나의 강의에 있던 여러개의 태그를 1:1로 붙혀서 저장
				.toList();

			lectureSkillTagRepository.saveAll(lectureSkillTags);
		}
		log.info("크롤링 완료");
	}
}

