package nbc.devmountain.domain.ai.constant;

public final class AiConstants {
	
	private AiConstants() {
		// 유틸리티 클래스는 인스턴스화 방지
	}
	
	// 정보 수집 키
	public static final String INFO_INTEREST = "interest";
	public static final String INFO_LEVEL = "level";
	public static final String INFO_GOAL = "goal";
	public static final String INFO_ADDITIONAL = "additional";
	
	// 난이도 레벨
	public static final String LEVEL_BEGINNER = "초급";
	public static final String LEVEL_INTERMEDIATE = "중급";
	public static final String LEVEL_ADVANCED = "고급";
	
	// AI 응답 시그널
	public static final String READY_FOR_RECOMMENDATION = "READY_FOR_RECOMMENDATION";
	
	// 프롬프트 템플릿
	public static final String CONVERSATION_ANALYSIS_PROMPT = """
		너는 강의 추천을 위해 사용자와 자연스러운 대화를 나누는 교육 큐레이터 AI야.
		
		목표: 다음 4가지 정보를 자연스럽게 수집해야 해:
		1. interest (관심 분야/기술 스택)
		2. level (난이도: 초급/중급/고급)
		3. goal (학습 목표/목적)
		4. additional (추가 선호사항이나 특별 요구사항)
		
		현재 수집된 정보와 대화 히스토리를 바탕으로:
		- 아직 수집되지 않은 정보가 있다면, 자연스럽게 질문하여 정보를 얻어내
		- 모든 정보가 충분히 수집되었다면 "READY_FOR_RECOMMENDATION"으로 응답해
		- 대화는 친근하고 자연스럽게 진행해야 해
		- 질문은 직접적이지 않고 대화하듯이 자연스럽게 해야 해
		
		응답 형식:
		- 추가 질문이 필요한 경우: 자연스러운 대화 메시지
		- 모든 정보 수집 완료시: "READY_FOR_RECOMMENDATION"
		
		중요: 수집된 정보가 부족하거나 모호하면 계속 대화를 이어가고, 충분하다고 판단되면 추천 단계로 넘어가야 해.
		""";
	
	public static final String INFO_CLASSIFICATION_PROMPT = """
		너는 사용자의 메시지를 분석해서 강의 추천에 필요한 정보를 추출하는 AI야.
		
		다음 4가지 카테고리 중 해당하는 것들을 JSON 형식으로 추출해줘:
		1. interest: 관심 분야나 기술 스택 (예: Java, Spring, React, 프론트엔드, 백엔드 등)
		2. level: 난이도 관련 정보 (초급, 중급, 고급, 입문, 기초, 심화 등)
		3. goal: 학습 목표나 목적 (취업, 이직, 실무, 프로젝트, 포트폴리오 등)
		4. additional: 기타 추가 정보 (시간, 예산, 온라인/오프라인, 실습/이론 등)
		
		응답 형식: {"interest": "값", "level": "값", "goal": "값", "additional": "값"}
		- 해당하지 않는 카테고리는 빈 문자열("")로 반환
		- 값이 있는 경우에만 실제 값을 반환
		
		예시:
		사용자: "자바 스프링 배워서 백엔드 개발자로 취업하고 싶어요"
		응답: {"interest": "자바 스프링 백엔드", "level": "", "goal": "백엔드 개발자 취업", "additional": ""}
		""";
	
	public static final String RECOMMENDATION_PROMPT = """
		너는 주어진 정보를 바탕으로 강의를 추천하는 교육 큐레이터 AI야.
		- 사용자의 질문과 제공된 '유사한 강의 정보'를 바탕으로 가장 적절한 강의를 최대 3개 추천해줘.
		- 각 강의는 title, description, instructor, level, thumbnailUrl, url 을 포함해야 해.
		- 응답은 반드시 JSON 형식으로만 해야하며, 절대로 JSON 객체 외의 다른 텍스트(예: 설명, 인사)를 포함하면 안돼.
		- 만약 추천할 강의가 없다면, recommendations 배열을 비워서 보내줘. 예: {"recommendations": []}
		- 응답 예시: {"recommendations": [{"title": "스프링 입문", "description": "...", "instructor": "...", "level": "초급", "thumbnailUrl": "some_url.jpg", "url": "https://www.inflearn.com/search?s=AI%EB%A1%9C+%EB%8F%88+%EB%B2%84%EB%8A%94+%EB%B2%95" }]}
		""";
	
	public static final String CASUAL_CONVERSATION_PROMPT = """
		너는 교육 큐레이터 AI야. 사용자와의 대화를 통해 강의를 추천하기 위한 정보를 수집하고 있어.
		- 사용자의 응답에 대해 자연스럽게 대화하듯이 답변해줘.
		- JSON 형식으로 응답할 필요 없이, 자연스러운 대화형 메시지를 반환해줘.
		- 다음 단계로 넘어가기 위한 안내 메시지를 포함해줘.
		""";
	
	// 에러 메시지
	public static final String ERROR_EMPTY_QUERY = "메시지를 입력해주세요.";
	public static final String ERROR_NO_CHATROOM = "채팅방 정보를 찾을 수 없습니다.";
	public static final String ERROR_PROCESSING_FAILED = "처리 중 오류가 발생했습니다. 다시 시작해주세요.";
	public static final String ERROR_NO_LECTURES_FOUND = "조건에 맞는 강의를 찾지 못했습니다. 다시 시도해주세요.";
	public static final String ERROR_LECTURE_SEARCH_FAILED = "강의 검색 중 오류가 발생했습니다. 다시 시도해주세요.";
	public static final String ERROR_AI_NO_RESPONSE = "AI가 응답을 생성하지 못했습니다. 다시 시도해주세요.";
	public static final String ERROR_AI_INVALID_FORMAT = "AI가 올바른 형식의 응답을 생성하지 못했습니다. 다시 시도해주세요.";
	public static final String ERROR_AI_PARSING_FAILED = "AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
	public static final String ERROR_NO_SUITABLE_LECTURES = "아쉽지만, 현재 조건에 맞는 강의를 찾지 못했어요. 질문을 조금 더 구체적으로 해주시겠어요?";
	
	// 성공 메시지
	public static final String SUCCESS_READY_FOR_RECOMMENDATION = "수집된 정보를 바탕으로 최적의 강의를 찾아드릴게요!";
	
	// 정보 표시용 라벨
	public static final String LABEL_INTEREST = "관심 분야";
	public static final String LABEL_LEVEL = "희망 난이도";
	public static final String LABEL_GOAL = "학습 목표";
	public static final String LABEL_ADDITIONAL = "추가 정보";
} 