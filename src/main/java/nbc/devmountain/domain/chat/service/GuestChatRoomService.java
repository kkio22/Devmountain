package nbc.devmountain.domain.chat.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

@Service
public class GuestChatRoomService {
	// key: guestRoomId, value: 메시지 리스트 (필요하면 ChatMessageResponse 등)
	private final ConcurrentHashMap<String, List<String>> guestRooms = new ConcurrentHashMap<>();

	// 방 생성
	public void createRoom(String roomId) {
		guestRooms.putIfAbsent(roomId, new CopyOnWriteArrayList<>());
	}

	// 메시지 추가
	public void addMessage(String roomId, String message) {
		guestRooms.getOrDefault(roomId, new CopyOnWriteArrayList<>()).add(message);
	}

	// 메시지 조회
	public List<String> getMessages(String roomId) {
		return guestRooms.getOrDefault(roomId, List.of());
	}

	// 방 제거 (세션 종료 시 호출)
	public void removeRoom(String roomId) {
		guestRooms.remove(roomId);
	}

	// 방 존재 여부
	public boolean exists(String roomId) {
		return guestRooms.containsKey(roomId);
	}
}
