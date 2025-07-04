package nbc.devmountain.domain.lecture.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "websearch")
@NoArgsConstructor
public class WebSearch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String url;

	@Column(columnDefinition = "TEXT")
	private String thumbnailUrl;

	@Builder
	public WebSearch(String title, String description, String url, String thumbnailUrl) {
		this.title=title;
		this.description=description;
		this.url=url;
		this.thumbnailUrl=thumbnailUrl;
	}
}
