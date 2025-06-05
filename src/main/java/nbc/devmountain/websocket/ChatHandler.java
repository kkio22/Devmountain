package nbc.devmountain.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chat.model.ChatMessage;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.service.ChatMessageService;
import nbc.devmountain.domain.user.model.User;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

	private final ChatMessageService chatMessageService;

	/**메세지 입력시 호출되는 메서드*/
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		User user = (User)session.getAttributes().get("user"); //로그인 유저
		String payload = message.getPayload(); //사용자 입력메세지
		Long roomId = getRoomId(session);

		//비로그인 유저
		if(user==null){
		//검색 5회 제한 걸기
		}

		//사용자 요청 저장
		ChatMessageResponse userMessage = chatMessageService.createMessage(user, roomId, payload);

		//AI 응답 생성

		//AI 응답 저장
		// chatMessageService.save

		session.sendMessage(new TextMessage(
			buildJsonMessage()//
		));
	}


	/**uri 어떤 채팅방 id로 들어왔는지*/
	private Long getRoomId(WebSocketSession session) {

		String query = session.getUri().getQuery();
		String[] params = query.split("=");
		try{
			return Long.parseLong(params[1]);
		}catch (Exception e){
			throw new IllegalArgumentException("Invalid roomId in URI: " + query);
		}
	}
	/**AI 응답 JSON 형식으로 파싱*/
	public String buildJsonMessage(ChatMessage msg){
		try {
			return new ObjectMapper().writeValueAsString(ChatMessageResponse.from(msg));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("JSON 변환실패",e);
		}
	}
}

