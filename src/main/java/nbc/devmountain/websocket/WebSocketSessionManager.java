package nbc.devmountain.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionManager {

	private final Map<Long, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
	private final Map<String, Long> sessionToRoom = new ConcurrentHashMap<>();

	/**세션 등록*/
	public void addSession(Long roomId, WebSocketSession session) {
		activeSessions.put(roomId, session);
		sessionToRoom.put(session.getId(), roomId);
	}

	/**세션 제거*/
	public void removeSession(WebSocketSession session) {
		String sessionId = session.getId();
		Long roomId = sessionToRoom.remove(sessionId);
		if (roomId != null) {
			activeSessions.remove(roomId);

		}
	}

	public WebSocketSession getSession(Long roomId) {
		return activeSessions.get(roomId);
	}

	public boolean isSessionActive(Long roomId) {
		WebSocketSession session = activeSessions.get(roomId);
		return session != null && session.isOpen();
	}

	public int getActiveSessionCount() {
		return activeSessions.size();
	}
	/**채팅방 ID 조회*/
	public Long getRoomId(WebSocketSession session) {
		return sessionToRoom.get(session.getId());
	}
}
