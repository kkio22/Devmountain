package nbc.devmountain.domain.chat.chatmessage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nbc.devmountain.domain.chat.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {


	@Query("SELECT c FROM ChatMessage c WHERE c.chatRoom.chatroomId = :roomId ORDER BY c.createdAt ASC")
	List<ChatMessage> findByChatRoomId(Long roomId);
}
