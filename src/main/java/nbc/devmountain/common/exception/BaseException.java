package nbc.devmountain.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    public abstract HttpStatus getStatus();

    public abstract String getMessage();

    public abstract ExceptionCode getExceptionCode();
}
