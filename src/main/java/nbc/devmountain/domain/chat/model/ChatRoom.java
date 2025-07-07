package nbc.devmountain.domain.chat.model;

import jakarta.persistence.*;
import lombok.*;
import nbc.devmountain.domain.user.model.User;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ChatRoom")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatroomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String chatroomName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomType type;

	@OneToMany(mappedBy = "chatRoom"
		, cascade = CascadeType.ALL
		, orphanRemoval = true)
	private List<ChatMessage> messages = new ArrayList<>();

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime deletedAt;

	public void addMessages(ChatMessage message) {
		messages.add(message);
		message.setChatRoom(this);
	}

	public void updateName(String newName) {
		this.chatroomName = newName;
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}
}