package nbc.devmountain.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.devmountain.domain.chat.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

	@Query("SELECT c FROM ChatRoom c WHERE c.user.userId = :userId AND c.deletedAt IS NULL")
	List<ChatRoom> findAllByUserId(@Param("userId") Long userId);


}
