package nbc.devmountain.domain.chat.model.chatmessage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.chat.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
}
