package nbc.devmountain.domain.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.LectureCategoryMapping;
@Repository
public interface LectureCategoryMappingRepository extends JpaRepository<LectureCategoryMapping, Long> {
}
