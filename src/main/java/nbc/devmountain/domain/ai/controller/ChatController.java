package nbc.devmountain.domain.ai.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nbc.devmountain.domain.ai.dto.AiRecommendationResponse;
import nbc.devmountain.domain.ai.service.AiService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final AiService aiService;

	// @PostMapping
	// public ResponseEntity<String> ask(@RequestBody String message) {
	// 	String answer = aiService.ask(message);
	// 	return ResponseEntity.ok(answer);
	// }

	@PostMapping("/recommend")
	public ResponseEntity<AiRecommendationResponse> recommendLectures(@RequestBody String message) {
		AiRecommendationResponse response = aiService.getRecommendations(message);
		return ResponseEntity.ok(response);
	}

}

