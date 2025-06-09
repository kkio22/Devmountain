package nbc.devmountain.domain.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {
	FREE("free"),
	PRO("pro");

	private final String description;
}
