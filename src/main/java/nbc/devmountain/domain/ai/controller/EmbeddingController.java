package nbc.devmountain.domain.ai.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nbc.devmountain.domain.ai.service.EmbeddingService;

@RestController
@RequiredArgsConstructor
public class EmbeddingController {

	private final EmbeddingService embeddingService;

	@GetMapping("/ai/embedding")
	public List<Double> embed(@RequestParam String message) {
		return embeddingService.getEmbedding(message);
	}
}

