package nbc.devmountain.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
}
