package nbc.devmountain.domain.ai.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final OpenAiChatClient chatClient;

	@PostMapping
	public ResponseEntity<String> ask(@RequestBody String message) {
		ChatResponse response = chatClient.call(new Prompt(message));
		return ResponseEntity.ok(response.getResult().getOutput().getContent());
	}
}

