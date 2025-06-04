package nbc.devmountain.domain.chat.model.chatroom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.user.model.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

	@Query("SELECT c FROM ChatRoom c WHERE c.user = :user AND c.deletedAt IS NULL")
	List<ChatRoom> findAllByUser(@Param("user") User user);
}
