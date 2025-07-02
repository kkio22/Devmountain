package nbc.devmountain.domain.chat.controller;

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
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.chat.dto.ChatMessageRequest;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.chat.service.ChatMessageService;
import nbc.devmountain.common.monitering.CustomMetrics;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatMessageController {

	private final ChatMessageService chatMessageService;
	private final CustomMetrics customMetrics;

	@PostMapping("/{chatroomId}/messages")
	public ResponseEntity<ApiResponse<ChatMessageResponse>> createMessage(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
		@RequestBody ChatMessageRequest request){

		customMetrics.incrementMessageCount(); // 모니터링(사용자 메세지 수 체크)
		ChatMessageResponse message = chatMessageService.createMessage(customUserPrincipal.getUserId(), chatroomId, request.message());
		return ResponseEntity.ok(
			ApiResponse.of(true,"메세지 생성", HttpStatus.OK.value(), message));
	}

	@GetMapping("/{chatroomId}/messages")
	public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal CustomUserPrincipal customUserPrincipal){

		List<ChatMessageResponse> messages = chatMessageService.getMessages(customUserPrincipal.getUserId(), chatroomId);
		return ResponseEntity.ok(
			ApiResponse.of(true,"채팅방 메세지조회",HttpStatus.OK.value(), messages));

	}
}
