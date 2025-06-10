package nbc.devmountain.domain.lecture.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "lecture")
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
	private double star;
	private String levelCode;
	private boolean isDiscount;
	private BigDecimal payPrice;
	private BigDecimal regularPrice;
	private boolean isFree;
	private BigDecimal discountRate;


	@Builder
	public Lecture(int itemId, String thumbnailUrl, String title, String instructor, String description, int reviewCount,
		int studentCount, int likeCount, double star, String levelCode, boolean isDiscount, BigDecimal payPrice, BigDecimal regularPrice, boolean isFree, BigDecimal discountRate) {
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