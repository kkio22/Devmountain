package nbc.devmountain.domain.lecture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.SkillTag;
@Repository
public interface SkillTagRepository extends JpaRepository<SkillTag, Long> {

	Optional<SkillTag> findByTitle(String title);

}
