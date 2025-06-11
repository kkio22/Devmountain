package nbc.devmountain.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.category.model.UserCategory;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {
	}
