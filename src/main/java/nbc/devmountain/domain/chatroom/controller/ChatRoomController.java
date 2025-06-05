package nbc.devmountain.domain.chatroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.chatroom.dto.request.ChatRoomRequest;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomDetailResponse;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chatroom.service.ChatRoomService;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody ChatRoomRequest request) {
		long userId = 1L;
		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.createChatRoom(userId, request.chatroomName())));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> findAllChatRooms() {
		long userId = 1L;

		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.findAllChatRooms(userId)));
	}

	@GetMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> findChatRoom(@PathVariable Long chatroomId) {
		long userId = 1L;

		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.findChatRoom(userId, chatroomId)));
	}

	@PatchMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomName(
		@PathVariable Long chatroomId,
		@RequestBody ChatRoomRequest request) {
		long userId = 1L;

		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.updateChatRoomName(userId, chatroomId, request.chatroomName())));
	}

	@DeleteMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<Void>> deleteChatRoom(@PathVariable Long chatroomId) {
		long userId = 1L;
		chatRoomService.deleteChatRoom(userId, chatroomId);
		return ResponseEntity.ok(
			ApiResponse.of(
				true,
				"채팅방이 삭제되었습니다."
				, 200
				, null)
		);
	}

}

