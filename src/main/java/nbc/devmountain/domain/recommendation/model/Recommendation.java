package nbc.devmountain.domain.recommendation.model;

import jakarta.persistence.*;
import lombok.*;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.user.model.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity
@Table(name = "Recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Recommendation {
    @Id
    private String recommendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id")
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private Float score;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
