package nbc.devmountain.domain.chat.model.chatmessage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.chat.model.chatmessage.dto.request.ChatMessageRequest;
import nbc.devmountain.domain.chat.model.chatmessage.dto.response.ChatMessageResponse;
import nbc.devmountain.domain.chat.model.chatmessage.service.ChatMessageService;
import nbc.devmountain.domain.user.model.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	//1)메세지 생성시 채팅방이 생성
	//2)채팅방 id를 입력하고 채팅을 붙여나감
	@PostMapping("/{chatroomId}/messages")
	public ResponseEntity<ApiResponse<ChatMessageResponse>> createMessage(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal User user,
		@RequestBody ChatMessageRequest request){

		ChatMessageResponse message = chatMessageService.createMessage(user.getUserId(), chatroomId, request.getMessage());
		return ResponseEntity.ok(
			ApiResponse.of(true,"메세지 생성", HttpStatus.OK.value(), message));
	}

	@GetMapping("/{chatroomId}/messages")
	public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal User user){

		List<ChatMessageResponse> messages = chatMessageService.getMessages(user.getUserId(), chatroomId);
		return ResponseEntity.ok(
			ApiResponse.of(true,"채팅방 메세지조회",HttpStatus.OK.value(), messages));

	}
}
