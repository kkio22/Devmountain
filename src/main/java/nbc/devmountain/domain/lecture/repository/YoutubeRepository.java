package nbc.devmountain.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.lecture.model.Youtube;

public interface YoutubeRepository extends JpaRepository<Youtube, Long>{
	Youtube findByUrl(String url);
}
