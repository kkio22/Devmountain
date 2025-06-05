package nbc.devmountain.domain.chatroom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.chat.model.ChatRoom;
import nbc.devmountain.domain.user.model.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
	List<ChatRoom> findAllByUser(User user);
}
