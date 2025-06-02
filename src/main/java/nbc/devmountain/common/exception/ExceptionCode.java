package nbc.devmountain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    INVALID_INPUT("INVALID_INPUT", 400, "요청 값이 올바르지 않습니다."),
    ACCESS_DENIED("ACCESS_DENIED", 403, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500, "서버 내부 오류가 발생했습니다.");

    private final String error;
    private final int status;
    private final String message;
}