package nbc.devmountain.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketSessionManager {

	private final Map<Long, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
	private final Map<String, Long> sessionToRoom = new ConcurrentHashMap<>();

	/**세션 등록*/
	public void addSession(Long roomId, WebSocketSession session) {
		if (activeSessions.containsKey(roomId)) {
			WebSocketSession existingSession = activeSessions.get(roomId);
			try {
				existingSession.close();
			} catch (Exception e) {
				log.error("기존 세션 종료 중 오류 발생. sessionId={}", existingSession.getId(), e);
			}
		}
		activeSessions.put(roomId, session);
		sessionToRoom.put(session.getId(), roomId);
	}

	/**세션 제거*/
	public void removeSession(WebSocketSession session) {
		String sessionId = session.getId();
		Long roomId = sessionToRoom.remove(sessionId);
		if (roomId != null) {
			WebSocketSession existingSession = activeSessions.get(roomId);
			if (existingSession != null && existingSession.getId().equals(sessionId)) {
				activeSessions.remove(roomId);
			}
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
