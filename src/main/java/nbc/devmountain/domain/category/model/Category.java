package nbc.devmountain.domain.category.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    private CategoryName name;

    public enum CategoryName {
        FRONTEND, BACKEND, MOBILE, AI, ETC
    }
}
