package nbc.devmountain.domain.chatroom.controller;

import java.util.List;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.chatroom.dto.request.CreateChatRoomRequest;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomDetailResponse;
import nbc.devmountain.domain.chatroom.dto.response.ChatRoomResponse;
import nbc.devmountain.domain.chatroom.service.ChatRoomService;

@Controller
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody CreateChatRoomRequest request) {
		long userId = 1L;
		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.createChatRoom(userId,request.getChatroomName())));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> findAllChatRooms(){
		long userId = 1L;

		return ResponseEntity.ok(
			ApiResponse.success(chatRoomService.findAllChatRooms(userId)));
	}


}
