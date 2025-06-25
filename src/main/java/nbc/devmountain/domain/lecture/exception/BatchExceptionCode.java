package nbc.devmountain.domain.lecture.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.exception.ExceptionCode;

@RequiredArgsConstructor
public enum BatchExceptionCode implements ExceptionCode {

	LECTURE_API_FAILED("404", "LECTURE_API_FAILED",  HttpStatus.NOT_FOUND.value());

	private final String error;
	private final String message;
	private final int status;


	@Override
	public String getError() {
		return "";
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public String getMessage() {
		return "";
	}
}
