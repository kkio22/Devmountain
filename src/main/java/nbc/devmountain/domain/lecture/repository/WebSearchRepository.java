package nbc.devmountain.domain.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.lecture.model.WebSearch;

public interface WebSearchRepository extends JpaRepository<WebSearch, Long> {
}
