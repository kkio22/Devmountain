package nbc.devmountain.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>  {
}
