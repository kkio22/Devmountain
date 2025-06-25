package nbc.devmountain.domain.ai.constant;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AiConstants 상수 클래스 테스트")
class AiConstantsTest {

	@Test
	@DisplayName("정보 수집 키 상수들이 올바르게 정의됨")
	void shouldHaveCorrectInfoKeys() {
		// then
		assertThat(AiConstants.INFO_INTEREST).isEqualTo("interest");
		assertThat(AiConstants.INFO_LEVEL).isEqualTo("level");
		assertThat(AiConstants.INFO_GOAL).isEqualTo("goal");
		assertThat(AiConstants.INFO_ADDITIONAL).isEqualTo("additional");
	}

	@Test
	@DisplayName("난이도 레벨 상수들이 올바르게 정의됨")
	void shouldHaveCorrectLevelConstants() {
		// then
		assertThat(AiConstants.LEVEL_BEGINNER).isEqualTo("초급");
		assertThat(AiConstants.LEVEL_INTERMEDIATE).isEqualTo("중급");
		assertThat(AiConstants.LEVEL_ADVANCED).isEqualTo("고급");
	}

	@Test
	@DisplayName("AI 응답 시그널 상수가 올바르게 정의됨")
	void shouldHaveCorrectSignalConstant() {
		// then
		assertThat(AiConstants.READY_FOR_RECOMMENDATION).isEqualTo("READY_FOR_RECOMMENDATION");
	}

	@Test
	@DisplayName("프롬프트 템플릿 상수들이 비어있지 않음")
	void shouldHaveNonEmptyPromptTemplates() {
		// then
		assertThat(AiConstants.CONVERSATION_ANALYSIS_PROMPT).isNotBlank();
		assertThat(AiConstants.INFO_CLASSIFICATION_PROMPT).isNotBlank();
		assertThat(AiConstants.RECOMMENDATION_PROMPT).isNotBlank();
		assertThat(AiConstants.CASUAL_CONVERSATION_PROMPT).isNotBlank();
	}

	@Test
	@DisplayName("에러 메시지 상수들이 비어있지 않음")
	void shouldHaveNonEmptyErrorMessages() {
		// then
		assertThat(AiConstants.ERROR_EMPTY_QUERY).isNotBlank();
		assertThat(AiConstants.ERROR_NO_CHATROOM).isNotBlank();
		assertThat(AiConstants.ERROR_PROCESSING_FAILED).isNotBlank();
		assertThat(AiConstants.ERROR_NO_LECTURES_FOUND).isNotBlank();
		assertThat(AiConstants.ERROR_LECTURE_SEARCH_FAILED).isNotBlank();
		assertThat(AiConstants.ERROR_AI_NO_RESPONSE).isNotBlank();
		assertThat(AiConstants.ERROR_AI_INVALID_FORMAT).isNotBlank();
		assertThat(AiConstants.ERROR_AI_PARSING_FAILED).isNotBlank();
		assertThat(AiConstants.ERROR_NO_SUITABLE_LECTURES).isNotBlank();
	}

	@Test
	@DisplayName("성공 메시지 상수가 비어있지 않음")
	void shouldHaveNonEmptySuccessMessage() {
		// then
		assertThat(AiConstants.SUCCESS_READY_FOR_RECOMMENDATION).isNotBlank();
	}

	@Test
	@DisplayName("라벨 상수들이 올바르게 정의됨")
	void shouldHaveCorrectLabelConstants() {
		// then
		assertThat(AiConstants.LABEL_INTEREST).isEqualTo("관심 분야");
		assertThat(AiConstants.LABEL_LEVEL).isEqualTo("희망 난이도");
		assertThat(AiConstants.LABEL_GOAL).isEqualTo("학습 목표");
		assertThat(AiConstants.LABEL_ADDITIONAL).isEqualTo("추가 정보");
	}

	@Test
	@DisplayName("프롬프트에 필요한 키워드들이 포함되어 있음")
	void shouldHaveRequiredKeywordsInPrompts() {
		// then
		assertThat(AiConstants.CONVERSATION_ANALYSIS_PROMPT)
			.contains("interest", "level", "goal", "additional");
		
		assertThat(AiConstants.INFO_CLASSIFICATION_PROMPT)
			.contains("JSON", "interest", "level", "goal", "additional");
		
		assertThat(AiConstants.RECOMMENDATION_PROMPT)
			.contains("recommendations", "title", "description");
	}
} 