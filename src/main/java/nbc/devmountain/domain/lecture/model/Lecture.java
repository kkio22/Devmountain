package nbc.devmountain.domain.lecture.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lecture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "itemId")
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
	//@JsonProperty("discount")
	private boolean isDiscount;
	private BigDecimal payPrice;
	private BigDecimal regularPrice;
	//@JsonProperty("free")
	private boolean isFree;
	private BigDecimal discountRate;
	@Column(nullable = false)
	private LocalDateTime crawledAt;

	@JsonCreator
	@Builder
	public Lecture(@JsonProperty("itemId") int itemId,
		@JsonProperty("thumbnailUrl") String thumbnailUrl,
		@JsonProperty("title") String title,
		@JsonProperty("instructor") String instructor,
		@JsonProperty("description") String description,
		@JsonProperty("reviewCount") int reviewCount,
		@JsonProperty("studentCount") int studentCount,
		@JsonProperty("likeCount") int likeCount,
		@JsonProperty("star") double star,
		@JsonProperty("levelCode") String levelCode,
		@JsonProperty("discount") boolean isDiscount,
		@JsonProperty("payPrice") BigDecimal payPrice,
		@JsonProperty("regularPrice") BigDecimal regularPrice,
		@JsonProperty("free") boolean isFree,
		@JsonProperty("discountRate") BigDecimal discountRate,
		@JsonProperty("crawledAt") LocalDateTime crawledAt) {
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
		this.discountRate = discountRate;
		this.crawledAt = crawledAt;

	}

}