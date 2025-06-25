package nbc.devmountain.domain.chat.websocket;

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
	//채팅방 삭제시 해당 채팅방의 세션 끊기
	public void removeRoomSessions(Long roomId) {
		WebSocketSession session = activeSessions.remove(roomId);
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				log.error("채팅방 세션 종료 중 오류 발생. roomId={}, sessionId={}", roomId, session.getId(), e);
			}
			sessionToRoom.remove(session.getId());
		}
	}
}
