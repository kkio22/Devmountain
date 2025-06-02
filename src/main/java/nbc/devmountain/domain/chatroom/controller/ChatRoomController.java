package nbc.devmountain.domain.chatroom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.chatroom.service.ChatRoomService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
}
