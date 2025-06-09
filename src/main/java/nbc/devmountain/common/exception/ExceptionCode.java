package nbc.devmountain.common.exception;

public interface ExceptionCode {
    String getError();
    int getStatus();
    String getMessage();
}