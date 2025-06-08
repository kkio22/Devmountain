package nbc.devmountain.domain.category.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>  {
	Optional<Category> findByName(Category.CategoryName name);
}
