package nbc.devmountain.common.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    protected BaseException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public int getStatus() {
        return exceptionCode.getStatus();
    }

    @Override
    public String getMessage() {
        return exceptionCode.getMessage();
    }
}