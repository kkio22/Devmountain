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
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private Float score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureType type;

    @Column(columnDefinition = "TEXT")
    private String externalLectureJson;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    public enum LectureType {
        VECTOR, // 내부 DB 강의
        YOUTUBE,// 유튜브 강의
        BRAVE//브레이브서치 강의
    }
}
