package nbc.devmountain.common.exception;

public class ServerException extends BaseException {
    public ServerException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}