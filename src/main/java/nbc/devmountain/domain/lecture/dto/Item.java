package nbc.devmountain.domain.lecture.dto;

public record Item(
	int id,
	Course course,
	Instructor instructor,
	ListPrice listPrice

) {
}
