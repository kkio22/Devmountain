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
	private Long lectureId; // DB pk
	private int itemId;
	@Column(columnDefinition = "TEXT")
	private String thumbnailUrl;
	private String title;
	private String instructor;
	@Column(columnDefinition = "TEXT")
	private String description;
	private int reviewCount;
	private int studentCount;
	private int likeCount;
	private int star;
	private String levelCode;
	private boolean isDiscount;
	private int payPrice;
	private int regularPrice;
	private boolean isFree;
	private int discountRate;



	@Builder
	public Lecture(int itemId, String thumbnailUrl, String title, String instructor, String description, int reviewCount,
		int studentCount, int likeCount, int star, String levelCode, boolean isDiscount, int payPrice, int regularPrice, boolean isFree, int discountRate) {
		this.itemId = itemId;
		this.thumbnailUrl = thumbnailUrl;
		this.title = title;
		this.instructor = instructor;
		this.description = description;
		this.reviewCount = reviewCount;
		this.studentCount = studentCount;
		this.likeCount = likeCount;
		this.star = star;
		this.levelCode = levelCode;
		this.isDiscount = isDiscount;
		this.payPrice = payPrice;
		this.regularPrice = regularPrice;
		this.isFree = isFree;
		this.discountRate =discountRate;

	}

}