package nbc.devmountain.domain.chat.model.chatmessage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.chat.model.chatmessage.dto.request.ChatMessagRequest;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.service.ChatMessageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@PostMapping({"/messages","/{chatroomId}/messages"})
	public ResponseEntity<ApiResponse<ChatMessageResponse>> createMessage(
		@PathVariable(required = false) Long chatroomId,
		@RequestBody ChatMessagRequest request){
		long userId=1L;
		chatMessageService.createMessage(userId,chatroomId,request.getMessage());
		return null;
	}
}
