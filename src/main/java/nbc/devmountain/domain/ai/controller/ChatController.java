package nbc.devmountain.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nbc.devmountain.domain.ai.service.LectureRecommendationService;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.user.model.User;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final LectureRecommendationService lectureRecommendationService;

	@PostMapping("/recommend/{chatRoomId}")
	public ResponseEntity<ChatMessageResponse> recommendLectures(
		@PathVariable Long chatRoomId,
		@RequestBody String query
	) {
		User.MembershipLevel membershipType = User.MembershipLevel.GUEST;
		ChatMessageResponse response = lectureRecommendationService.recommendationResponse(query, membershipType, chatRoomId);

		return ResponseEntity.ok(response);
	}
}