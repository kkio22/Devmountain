package nbc.devmountain.domain.chat.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {
	FREE("free"),
	PRO("pro");

	private final String description;
}
