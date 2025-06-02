package nbc.devmountain.domain.chatroom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chatroom.dto.request.CreateChatRoomRequest;
import nbc.devmountain.domain.chatroom.service.ChatRoomService;

@Controller
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping()
	public ResponseEntity<?> createChatRoom(@RequestBody CreateChatRoomRequest request) {
		long userId = 1L;

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(chatRoomService.createChatRoom(userId,request.getChatroomName()));
	}


}
