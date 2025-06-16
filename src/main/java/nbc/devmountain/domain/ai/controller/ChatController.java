package nbc.devmountain.domain.ai.controller;

import lombok.RequiredArgsConstructor;

import nbc.devmountain.common.util.security.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nbc.devmountain.domain.ai.service.LectureRecommendationService;
import nbc.devmountain.domain.chat.dto.ChatMessageResponse;
import nbc.devmountain.domain.user.model.User;

import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final LectureRecommendationService lectureRecommendationService;

	@PostMapping("/recommend/{chatRoomId}")
	public ResponseEntity<ChatMessageResponse> recommendLectures(
		@PathVariable Long chatRoomId,
		@RequestBody String query,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {

		// 비회원일 경우 GUEST 회원일 경우 해당 멤버십 레벨로 요청 진행
		User.MembershipLevel membershipLevel = Optional.ofNullable(principal)
				.map(CustomUserPrincipal::getMembershipLevel)
				.orElse(User.MembershipLevel.GUEST);

		ChatMessageResponse response = lectureRecommendationService.recommendationResponse(query, membershipLevel, chatRoomId);

		return ResponseEntity.ok(response);
	}
}