package nbc.devmountain.domain.chat.model.chatroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import nbc.devmountain.domain.chat.model.chatroom.dto.request.ChatRoomRequest;
import nbc.devmountain.domain.chat.model.chatroom.dto.response.ChatRoomDetailResponse;
import nbc.devmountain.domain.chat.model.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chat.model.chatroom.service.ChatRoomService;
import nbc.devmountain.domain.user.model.User;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
		@AuthenticationPrincipal User user,
		@RequestBody ChatRoomRequest request) {

		ChatRoomResponse chatRoom = chatRoomService.createChatRoom(user, request.chatroomName());
		return ResponseEntity.ok(
			ApiResponse.of(true, "채팅방 생성 성공", HttpStatus.CREATED.value(), chatRoom));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> findAllChatRooms(
		@AuthenticationPrincipal User user) {

		List<ChatRoomResponse> chatRooms = chatRoomService.findAllChatRooms(user);
		return ResponseEntity.ok(
			ApiResponse.of(true, "채팅방 목록 조회", HttpStatus.OK.value(), chatRooms));
	}

	@GetMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> findChatRoom(
		@AuthenticationPrincipal User user,
		@PathVariable Long chatroomId) {

		ChatRoomDetailResponse chatRoom = chatRoomService.findChatRoom(user, chatroomId);
		return ResponseEntity.ok(
			ApiResponse.of(true, "채팅방 상세 조회", HttpStatus.OK.value(), chatRoom));
	}

	@PatchMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomName(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal User user,
		@RequestBody ChatRoomRequest request) {

		ChatRoomResponse response = chatRoomService.updateChatRoomName(user, chatroomId,
			request.chatroomName());
		return ResponseEntity.ok(
			ApiResponse.of(true, "채팅방 이름 수정 완료", HttpStatus.OK.value(), response));
	}

	@DeleteMapping("/{chatroomId}")
	public ResponseEntity<ApiResponse<Void>> deleteChatRoom(
		@PathVariable Long chatroomId,
		@AuthenticationPrincipal User user) {

		chatRoomService.deleteChatRoom(user, chatroomId);
		return ResponseEntity.ok(
			ApiResponse.of(true, "채팅방이 삭제되었습니다.", HttpStatus.OK.value(), null)
		);
	}

}

