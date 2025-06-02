package nbc.devmountain.domain.lecture.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import nbc.devmountain.domain.category.model.*;

@Entity
@Table(name = "Lecture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    private String name;
    private String summary;
    private String instructor;
    private Float price;
    private String lectureImage;

    @Builder
    public Lecture(Category category, String name, String summary, String instructor, Float price, String lectureImage) {
        this.category = category;
        this.name = name;
        this.summary = summary;
        this.instructor = instructor;
        this.price = price;
        this.lectureImage = lectureImage;
    }
}