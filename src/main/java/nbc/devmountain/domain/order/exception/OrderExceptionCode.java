package nbc.devmountain.domain.order.exception;

import nbc.devmountain.common.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum OrderExceptionCode implements ExceptionCode {
    ORDER_NOT_FOUND("404", "주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    USER_NOT_FOUND("404", "유저 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    NO_PERMISSION("403", "해당 주문에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    PAYMENT_FAILED("404", "결제를 실패했습니다.", HttpStatus.NOT_FOUND.value()),;

    private final String error;
    private final String message;
    private final int status;

    OrderExceptionCode(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }

    @Override public String getError() { return error; }
    @Override public String getMessage() { return message; }
    @Override public int getStatus() { return status; }
}