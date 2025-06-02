package nbc.devmountain.domain.chatroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.chat.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
}
